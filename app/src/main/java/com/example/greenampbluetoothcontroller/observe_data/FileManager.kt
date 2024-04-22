package com.example.greenampbluetoothcontroller.observe_data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.util.AppConstants.writeExternalStorage
import com.example.greenampbluetoothcontroller.util.hasPermission
import java.io.File

const val csvDataType = "text/csv"
const val openFile = "openFile"

fun openFileInternal(
    context: Context,
    file: File,
    dataType: String = csvDataType
) {
    val target = Intent(Intent.ACTION_VIEW)
    target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
    try {
        val uri = FileProvider.getUriForFile(
            context,
            fileProvider,
            file
        )

        target.setDataAndType(uri, dataType)
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(target, openFile))
    } catch (e: Exception) {
        e.printStackTrace()
        Log.i("Error", "openFileInternal: Something went wrong!")
    }
}


fun Fragment.hasStorageAccess(onPermissionGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
        onPermissionGranted()
        return
    }
    if (requireContext().hasPermission(writeExternalStorage)) onPermissionGranted()
    else checkGranted { onPermissionGranted() }
}


fun Fragment.checkGranted(onPermissionGranted: () -> Unit) {

    val settingsBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkGranted(onPermissionGranted)
        }

    val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                handlePermissionDenied("Storage permission is permanently denied. Please enable it from the app settings") {
                    settingsBluetoothLauncher.launch(it)
                }
            }
        }

    requestStoragePermissionLauncher.launch(writeExternalStorage)

}


private fun Fragment.handlePermissionDenied(
    message: String = "Bluetooth and GPS permissions have been permanently denied. Please enable them from the app settings",
    onSettingsClicked: (settingsIntent: Intent) -> Unit
) {

    val alert = android.app.AlertDialog.Builder(requireContext())

    alert.create()

    alert.setTitle("Permission Required")

    alert.setIcon(R.drawable.ic_bluetooth_search_24)

    alert.setMessage(message)

    alert.setPositiveButton("Go to Settings") { _, _ ->
        val settingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireActivity().packageName}")
        )
        onSettingsClicked(settingsIntent)
    }

    alert.setCancelable(false)

    alert.show()

}
