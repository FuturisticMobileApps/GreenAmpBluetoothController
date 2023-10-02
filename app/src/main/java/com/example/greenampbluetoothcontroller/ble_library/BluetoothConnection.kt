package com.example.greenampbluetoothcontroller.ble_library

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import com.example.greenampbluetoothcontroller.ble_library.enums.Priority
import com.example.greenampbluetoothcontroller.ble_library.enums.GattState
import com.example.greenampbluetoothcontroller.ble_library.enums.GattStatus
import com.example.greenampbluetoothcontroller.ble_library.exceptions.ConnectionClosingException
import com.example.greenampbluetoothcontroller.ble_library.models.BluetoothCharacteristic
import com.example.greenampbluetoothcontroller.ble_library.models.BluetoothService
import com.example.greenampbluetoothcontroller.ble_library.typealiases.Callback
import com.example.greenampbluetoothcontroller.ble_library.typealiases.EmptyCallback
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

typealias OnCharacteristicValueChangeCallback<T> = (new: T) -> Unit
typealias OnCharacteristicValueReadCallback<T> = (new: T?) -> Unit

@Suppress("unused")
class BluetoothConnection internal constructor(private val device: BluetoothDevice) {

	/* Bluetooth */
	private var gatt: BluetoothGatt? = null
	private var closingConnection: Boolean = false
	private var connectionActive: Boolean = false
	private var priority: Priority = Priority.Balanced

	/* Callbacks */
	private var connectionCallback: Callback<Boolean>? = null

	/* Misc */
	private var operationsInQueue: AtomicInteger = AtomicInteger(0)
	private var activeObservers = hashMapOf<String, OnCharacteristicValueChangeCallback<ByteArray>>()
	private var enqueuedReadCallbacks = hashMapOf<String, OnCharacteristicValueReadCallback<ByteArray>>()

	internal var coroutineScope: CoroutineScope? = null

	/**
	 * Indicates whether additional information should be logged
	 **/
	var verbose: Boolean = false

	/**
	 * Called whenever a successful connection is established
	 **/
	var onConnect: EmptyCallback? = null

	/**
	 * Called whenever a connection is lost
	 **/
	var onDisconnect: EmptyCallback? = null

	/**
	 * Indicates whether the connection is active
	 **/
	val isActive get() = this.connectionActive

	/**
	 * Indicates the connection signal strength
	 * <i>Measured in dBm</i>
	 **/
	var rsii: Int = 0
		private set

	/**
	 * Indicates the connection MTU, default 23
	 * <i>Measured in Bytes</i>
	 *
	 * @see https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/system/stack/include/gatt_api.h;l=543;drc=6cf6099dcab87865e33439215e7ea0087e60c9f2#:~:text=%23define%20GATT_DEF_BLE_MTU_SIZE%2023
	 **/
	var mtu: Int = 23
		private set

	/**
	 * Holds the discovered services
	 **/
	val services get() = this.gatt?.services?.map { BluetoothService(it) } ?: listOf()

	/** A list with all notifiable characteristics
	 * @see observe
	 **/
	val notifiableCharacteristics get() = this.dumpCharacteristics { it.isNotifiable }

	/** A list with all writable characteristics
	 * @see write
	 **/
	val writableCharacteristics get() = this.dumpCharacteristics { it.isWritable }

	/** A list with all readable characteristics
	 * @see read
	 **/
	val readableCharacteristics get() = this.dumpCharacteristics { it.isReadable }

	// region Utility related methods
	private fun log(message: String) {
		if (this.verbose) Log.d("BluetoothConnection", message)
	}

	private fun warn(message: String) {
		Log.w("BluetoothConnection", message)
	}

	private fun error(message: String) {
		Log.e("BluetoothConnection", message)
	}

	private fun dumpCharacteristics(filter: (BluetoothCharacteristic) -> Boolean): List<String> {
		return this.services.map { service ->
			service.characteristics.filter { characteristic ->
				filter(characteristic)
			}.map { characteristic ->
				characteristic.uuid.toString().lowercase()
			}
		}.flatten().distinct()
	}

	@SuppressLint("MissingPermission")
	private fun setupGattCallback(): BluetoothGattCallback {
		return object : BluetoothGattCallback() {

			override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, state: Int) {
				super.onConnectionStateChange(gatt, status, state)
        
        val mappedStatus = GattStatus.fromCode(status)
        val mappedState = GattState.fromCode(state)
				log("onConnectionStateChange: status $status $mappedStatus state $state $mappedState")

				if (state == BluetoothProfile.STATE_CONNECTED) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
					log("Device ${device.address} connected!")

					// Notifies that the connection has been established
					if (!connectionActive) {
						onConnect?.invoke()
						connectionActive = true
					}

					// Requests the connection priority
					if (gatt?.requestConnectionPriority(priority.toGattEnum()) != true) {
						log("Could not elevate connection priority to $priority!")
					}

					// Starts the services discovery
					this@BluetoothConnection.gatt?.discoverServices()
					} else {
						// Feedback of failed connection
          				log("Something went wrong while trying to connect... STATUS: $mappedStatus\nForcing disconnect...")

						// Start disconnection
						startDisconnection()
					}
				} else if (state == BluetoothProfile.STATE_DISCONNECTED) {
					if (status == 133) {// HACK: Multiple reconnections handler
						log("Found 133 connection failure! Reconnecting GATT...")
					} else if (closingConnection) {// HACK: Workaround for Lollipop 21 and 22
						endDisconnection()
					} else {
						// Notifies that the connection has been lost
						if (connectionActive) {
							log("Lost connection with ${device.address} STATUS: $mappedStatus")
							onDisconnect?.invoke()
							connectionActive = false
						}
					}
				}
			}

			override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
				super.onServicesDiscovered(gatt, status)

				coroutineScope?.launch {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						log("onServicesDiscovered: ${gatt?.services?.size ?: 0} services found!")
						connectionCallback?.invoke(true)
					} else {
						error("Error while discovering services at ${device.address}! Status: $status")
						close()
						connectionCallback?.invoke(false)
					}
				}
			}

			override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
				super.onReadRemoteRssi(gatt, rssi, status)

				// Update the internal rsii variable
				if (status == BluetoothGatt.GATT_SUCCESS) {
					log("onReadRemoteRssi: $rssi")
					this@BluetoothConnection.rsii = rssi
				} else {
					error("Error while reading RSSI at ${device.address}! Status: $status")
				}
			}

			override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
				super.onMtuChanged(gatt, mtu, status)

				// Update MTU value
				if (status == BluetoothGatt.GATT_SUCCESS) {
					log("onMtuChanged: $mtu")
					this@BluetoothConnection.mtu = mtu
				} else {
					error("Error while changing MTU at ${device.address}! Status: $status")
				}
			}

			@Deprecated("Deprecated in Java")
			override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
				super.onCharacteristicChanged(gatt, characteristic)
				if (characteristic == null) return

				log("onCharacteristicChanged: $characteristic")

				val key = characteristic.uuid.toString().lowercase()
				coroutineScope?.launch {
					activeObservers[key]?.invoke(characteristic.value)
				}
			}

			@Deprecated("Deprecated in Java")
			override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
				super.onCharacteristicRead(gatt, characteristic, status)
				if (characteristic == null) return

				val key = characteristic.uuid.toString().lowercase()
				var value: ByteArray? = null

				if (status == BluetoothGatt.GATT_SUCCESS) {
					log("onCharacteristicRead: '${characteristic.uuid}' read '$key'")
					value = characteristic.value
				} else {
					error("onCharacteristicRead: Error while reading '$key' status: $status")
				}

				// Invoke callback and remove it from the queue
				coroutineScope?.launch {
					enqueuedReadCallbacks[key]?.invoke(value)
					enqueuedReadCallbacks.remove(key)
				}
			}
		}
	}

	private fun getCharacteristic(gatt: BluetoothGatt, characteristic: String): BluetoothCharacteristic? {
		// Converts the specified string into an UUID
		val characteristicUuid = UUID.fromString(characteristic)

		// Iterates trough every service on the gatt
		gatt.services?.forEach { service ->
			// Iterate trough every characteristic on the service
			service.getCharacteristic(characteristicUuid)?.let {
				return BluetoothCharacteristic(it)
			}
		}

		error("Characteristic $characteristic not found on device ${device.address}!")
		return null
	}
	// endregion

	// region Operation queue related methods
	private fun beginOperation() {
		// Don't allow operations while closing an active connection
		if (closingConnection) throw ConnectionClosingException()

		// Increment the amount of operations
		this.operationsInQueue.incrementAndGet()
	}

	private fun finishOperation() {
		// Decrement the amount of operations, because the current operation has finished
		this.operationsInQueue.decrementAndGet()
	}
	// endregion

	// region Misc
	/**
	 * Request a MTU change
	 * Note: even if this method returns true, it is possible that the other device does not accept it, so Android will fallback to the previous negotiated MTU value.
	 * This will be reflected by the [mtu] variable.
	 *
	 * @see android.Manifest.permission.BLUETOOTH_CONNECT for Android 31+
	 *
	 * @return True when successfully requested MTU negotiation
	 **/
	@SuppressLint("MissingPermission")
	fun requestMTU(bytes: Int): Boolean {
		if (bytes < 0) {
			this.error("MTU size should be greater than 0!")
			return false
		}

		if (bytes > GATT_MAX_MTU) {
			this.warn("Requested MTU is over the recommended amount of $GATT_MAX_MTU. Are you sure?")
		}

		// Request for MTU change
		this.log("Request MTU on device: ${device.address} (${mtu} bytes)")
		val success = this.gatt?.requestMtu(mtu) ?: false
		if (success) {
			this.log("MTU change request success on: ${device.address} (value: $mtu)")
		} else {
			this.error("Could not request MTU change on: ${device.address}")
		}

		return success
	}

	/**
	 * Requests a read of the RSSI value, which is updated on the [rsii] variable
	 *
	 * @see android.Manifest.permission.BLUETOOTH_CONNECT for Android 31+
	 *
	 * @return True when successfully requested a RSSI read
	 **/
	@SuppressLint("MissingPermission")
	fun readRSSI(): Boolean {
		// Request RSSI read
		log("Requesting rssi read on: ${device.address}")
		val success = this.gatt?.readRemoteRssi() ?: false
		if (success) {
			log("RSSI read success on: ${device.address} (value: $rsii)")
		} else {
			error("Could not read rssi on: ${device.address}")
		}

		return success
	}
	// endregion

	// region Value writing related methods
	/**
	 * Performs a write operation on a specific characteristic
	 *
	 * @see [write] For a variant that receives a [String] value
	 *
	 * @param characteristic The uuid of the target characteristic
	 *
	 * @return True when successfully written the specified value
	 **/
	fun write(characteristic: String, message: ByteArray): Boolean {
		// TODO: Add reliable writing implementation
		this.log("Writing to device ${device.address} (${message.size} bytes)")
		this.beginOperation()

		if (message.size > this.mtu) {
			this.warn("Message being written exceeds MTU size!")
		}

		// Null safe let of the generic attribute profile
		this.gatt?.let { gatt ->
			// Searches for the characteristic
			getCharacteristic(gatt, characteristic)?.let {
				// Tries to write its value
				val success = it.write(gatt, message)
				if (success) {
					this.finishOperation()
					return true
				} else {
					log("Could not write to device ${device.address}")
				}
			}
		}

		this.finishOperation()
		return false
	}

	/**
	 * Performs a write operation on a specific characteristic
	 *
	 * @see [write] For a variant that receives a [ByteArray] value
	 *
	 * @param characteristic The uuid of the target characteristic
	 *
	 * @return True when successfully written the specified value
	 **/
	fun write(characteristic: String, message: String, charset: Charset = Charsets.UTF_8): Boolean = this.write(characteristic, message.toByteArray(charset))
	// endregion

	// region Value reading related methods
	/**
	 * Performs a read operation on a specific characteristic
	 *
	 * @see [read] For a variant that returns a [String] value
	 * @see readAsync For a variation using callbacks
	 *
	 * @param characteristic The uuid of the target characteristic
	 *
	 * @return A nullable [ByteArray], null when failed to read
	 **/
	suspend fun read(characteristic: String): ByteArray? {
		return suspendCancellableCoroutine { continuation ->
			readAsync(characteristic) {
				continuation.resume(it)
			}
		}
	}

	/**
	 * Performs a read operation on a specific characteristic
	 *
	 * @see [read] For a variant that returns a [ByteArray] value
	 * @see readAsync For a variation using callbacks
	 *
	 * @param characteristic The uuid of the target characteristic
	 * @param charset The charset to decode the received bytes
	 *
	 * @return A nullable [String], null when failed to read
	 **/
	suspend fun read(characteristic: String, charset: Charset = Charsets.UTF_8): String? = this.read(characteristic)?.let { String(it, charset) }

	/**
	 * Performs a read operation on a specific characteristic
	 *
	 * @see [read] For a variant that returns a [String] value
	 * @see [read] For a variation using coroutines suspended functions
	 *
	 * @param characteristic The uuid of the target characteristic
	 *
	 * @return A nullable [ByteArray], null when failed to read
	 **/
	fun readAsync(characteristic: String, callback: (ByteArray?) -> Unit) {
		// Check if the characteristic is already in queue to be read, and if so ignore
		if (this.enqueuedReadCallbacks.containsKey(characteristic)) {
			error("read: '$characteristic' already waiting for read, ignoring read request.")
			return callback(null)
		}

		// Null safe let of the generic attribute profile
		this.beginOperation()
		gatt?.let { gatt ->
			// Searches for the characteristic
			this.getCharacteristic(gatt, characteristic)?.let {
				if (!it.read(gatt)) {
					// The operation was not successful
					error("read: '$characteristic' error while starting the read request.")
					this.finishOperation()
					return callback(null)
				}

				// Define the callback to resume the coroutine
				this.enqueuedReadCallbacks[characteristic] = { response ->
					if (response != null) {
						callback(response)
					} else {
						// The operation was not successful
						error("read: '$characteristic' error while starting the read request.")
						callback(null)
					}

					this.finishOperation()
				}
			}
		}
	}

	/**
	 * Performs a read operation on a specific characteristic
	 *
	 * @see [read] For a variant that returns a [ByteArray] value
	 * @see [read] For a variation using coroutines suspended functions
	 *
	 * @param characteristic The uuid of the target characteristic
	 * @param charset The charset to decode the received bytes
	 *
	 * @return A nullable [String], null when failed to read
	 **/
	fun readAsync(characteristic: String, charset: Charset, callback: (String?) -> Unit) = readAsync(characteristic) { callback(it?.let { String(it, charset) }) }
	// endregion

	// region Value observation related methods
	private fun legacyObserve(owner: LifecycleOwner, characteristic: BluetoothCharacteristic, callback: OnCharacteristicValueChangeCallback<ByteArray>, interval: Long) {
		this.coroutineScope?.launch {
			var lastValue: ByteArray? = null

			// While the lifecycle owner is not destroyed and the observer is in the activeObservers
			val key = characteristic.uuid.toString().lowercase()
			var startTime: Long
			while (owner.lifecycle.currentState != Lifecycle.State.DESTROYED && activeObservers.containsKey(key)) {
				if (!enqueuedReadCallbacks.containsKey(key)) {
					// Read the characteristic
					startTime = System.currentTimeMillis()
					read(key)?.let { currentValue ->
						log("legacyObserve: [${currentValue.joinToString(", ")}]")

						// Check if it has changed
						if (!currentValue.contentEquals(lastValue)) {
							// It has, invoke the callback and store the current value
							if (lastValue != null) {
								callback.invoke(currentValue)
								log("legacyObserve: Value changed!")
							}

							lastValue = currentValue
						}

						// Calculate the elapsed time and subtract it from the interval time
						val endTime = System.currentTimeMillis()
						val elapsedTime = endTime - startTime
						val sleepTime = (interval - elapsedTime).coerceAtLeast(0L)

						// Updates too close can be harmful to the battery, warn the user
						if (sleepTime <= 0L) warn("The elapsed time $elapsedTime between reads exceeds the specified interval of ${interval}ms. You should consider increasing the interval!")

						delay(sleepTime)
					}
				}
			}
		}
	}

	/**
	 * Observe a given [characteristic]
	 * @see stopObserving
	 *
	 * If the characteristic does not contain property [BluetoothGattCharacteristic.PROPERTY_NOTIFY] it will try to poll the value
	 *
	 * @see observeString
	 *
	 * @param characteristic The characteristic to observe
	 * @param callback The lambda to be called whenever a change is detected
	 * @param interval *Optional* only used for characteristics that doesn't have the NOTIFY property, define the amount of time to wait between readings
	 * @param owner *Optional* only used for characteristics that doesn't have the NOTIFY property, define the lifecycle owner of the legacy observer really useful to avoid memory leaks
	 **/
	fun observe(characteristic: String, interval: Long = 5000, owner: LifecycleOwner? = null, callback: OnCharacteristicValueChangeCallback<ByteArray>) {
		// Generate an id for the observation
		this.activeObservers[characteristic.lowercase()] = callback

		this.gatt?.let { gatt ->
			this.getCharacteristic(gatt, characteristic)?.let {
				if (!it.isNotifiable && it.isReadable && owner != null && coroutineScope != null) {
					legacyObserve(owner, it, callback, interval)
				} else {
					it.enableNotify(gatt)
				}
			}
		}
	}

	/**
	 * Observe a given [characteristic]
	 * @see stopObserving
	 *
	 * If the characteristic does not contain property [BluetoothGattCharacteristic.PROPERTY_NOTIFY] it will try to poll the value
	 *
	 * @see observe
	 *
	 * @param characteristic The characteristic to observe
	 * @param callback The lambda to be called whenever a change is detected
	 * @param interval *Optional* only used for characteristics that doesn't have the NOTIFY property, define the amount of time to wait between readings
	 * @param owner *Optional* only used for characteristics that doesn't have the NOTIFY property, define the lifecycle owner of the legacy observer really useful to avoid memory leaks
	 **/
	fun observeString(characteristic: String, interval: Long = 5000, owner: LifecycleOwner? = null, charset: Charset = Charsets.UTF_8, callback: OnCharacteristicValueChangeCallback<String>) {
		this.observe(characteristic, interval = interval, owner = owner, callback = {
			callback.invoke(it.toString(charset))
		})
	}

	/**
	 * Stops observing the characteristic
	 * @see observeString
	 * @see observe
	 *
	 * @param characteristic The characteristic to observe
	 **/
	fun stopObserving(characteristic: String) {
		this.gatt?.let { gatt ->
			this.getCharacteristic(gatt, characteristic)?.disableNotify(gatt)
		}

		this.activeObservers.remove(characteristic.lowercase())
	}
	// endregion

	// region Workaround for lollipop
	@SuppressLint("ObsoleteSdkInt")
	private fun isLollipop() = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && VERSION.SDK_INT <= VERSION_CODES.M

	@SuppressLint("MissingPermission")
	private fun startDisconnection() {
		try {
			this.closingConnection = true
			this.gatt?.disconnect()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@SuppressLint("MissingPermission")
	private fun endDisconnection() {
		log("Disconnected succesfully from ${device.address}!\nClosing connection...")

		try {
			this.connectionActive = false
			this.connectionCallback?.invoke(false)
			this.onDisconnect?.invoke()
			this.gatt?.close()
			this.gatt = null
			this.closingConnection = false
		} catch (e: Exception) {
			log("Ignoring closing connection with ${device.address} exception -> ${e.message}")
		}
	}
	// endregion

	// region Connection handling related methods
  	@SuppressLint("MissingPermission")
	internal fun establish(context: Context, priority: Priority = Priority.Balanced, callback: Callback<Boolean>) {
		this.connectionCallback = {
			// Clear the operations queue
			closingConnection = false
			operationsInQueue.set(0)

			// Calls the external connection callback
			callback(it)
			connectionCallback = null
		}

		// Update the connection priority
		this.priority = priority

		// Check for bluetooth connect permission
		if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			error("Missing permission BLUETOOTH_CONNECT!")
			callback.invoke(false)
			return
		}

		// HACK: Android M+ requires a transport LE in order to skip the 133 of death status when connecting
		this.gatt = if (VERSION.SDK_INT >= VERSION_CODES.M)
			this.device.connectGatt(context, true, setupGattCallback(), BluetoothDevice.TRANSPORT_LE)
		else
			this.device.connectGatt(context, false, setupGattCallback())
	}

	/**
	 * Closes the connection
	 **/
	suspend fun close() {
		// Wait for ongoing operations to finish before closing the connection
		// Has a counter of 20 times 500ms each
		// Being 10s in total of timeout
		var counter = 0
		while (operationsInQueue.get() > 0 && counter < 20) {
			this.log("${operationsInQueue.get()} operations in queue! Waiting for 500ms (${counter * 500}ms elapsed)")

			delay(500)
			counter++
		}

		// HACK: Workaround for Lollipop 21 and 22
		this.startDisconnection()
	}
	// endregion

}