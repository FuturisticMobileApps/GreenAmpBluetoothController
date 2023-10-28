package com.example.greenampbluetoothcontroller.dataStore

import android.content.Context
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.deviceDataStore


    private val preferencesFlow = dataStore.data.map { preferences ->
        preferences[DevicePreferencesKeys.deviceJson] ?: ""
    }

    suspend fun updateDeviceList(deviceList: List<BLEDeviceLocal>) {
        dataStore.insertOrUpdate(DevicePreferencesKeys.deviceJson, fromListToString(deviceList))
    }

    suspend fun getDeviceList(): List<BLEDeviceLocal> {
        val jsonString = preferencesFlow.first()
        return fromStringToList(jsonString) ?: emptyList()
    }
}


fun <T> fromListToString(value: List<T>): String {
    return Gson().toJson(value)
}


inline fun <reified T> fromStringToList(value: String): List<T>? {
    val type: Type = object : TypeToken<List<T>>() {}.type
    return Gson().fromJson(value, type)
}

