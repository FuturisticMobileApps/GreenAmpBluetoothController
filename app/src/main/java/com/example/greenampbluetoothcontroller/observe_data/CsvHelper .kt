package com.example.greenampbluetoothcontroller.observe_data

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val filePath =
    Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOCUMENTS}/GreenAmp")

const val fileProvider = "com.example.greenampbluetoothcontroller.provider"


suspend fun generateAndSaveCsv(fileName: String, data: List<List<String>>) {

    withContext(Dispatchers.IO) {

        try {

            val folderPath =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}" + "/GreenAmp"
            val folder = File(folderPath)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            val file = File(folder, fileName)
            val writer = FileWriter(file)

            //Write 1st header
            writer.append("Battery No: ", fileName, "    ", "    ", "    ", "Time/Date", "${getCurrentTime()}\n")

            // Write header
            writer.append("data point,time (secs),raw current (mA),stack voltage (mV),power (watts),filtered current (mA), temperature (°C)\n")

            // Write data
            for (row in data) {
                writer.append(row.joinToString(","))
                writer.append("\n")
            }

            writer.flush()
            writer.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}

fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val currentTime = Date(System.currentTimeMillis())
    return dateFormat.format(currentTime)
}

fun getCurrentTimeFormatted(): String {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault())
    val currentTime = Date(System.currentTimeMillis())
    return dateFormat.format(currentTime)
}
