package com.example.greenampbluetoothcontroller.ui

import android.util.Log

object DataFormatter {

    fun getVoltage(byteArray: ByteArray): String {

        return try {


            val bytes = byteArrayOf(
                byteArray[0], byteArray[1], byteArray[2],
                byteArray[3]
            )
            val int = ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)

            Log.i("output", "getVoltage: $int")

            return int.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    fun getTemperature(byteArray: ByteArray): String {

        return try {

            val bytes = byteArrayOf(
                byteArray[4], byteArray[5], byteArray[6],
                byteArray[7]
            )

            val intBits = ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
            val float = Float.fromBits(intBits)

            println("The float value is: $float")

            return float.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    fun getCurrent(byteArray: ByteArray): String {

        return try {

            val bytes = byteArrayOf(
                byteArray[8], byteArray[9], byteArray[10],
                byteArray[11]
            )

            val intBits = ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
            val float = Float.fromBits(intBits)

            println("The float value is: $float")

            return float.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    fun getSOC(byteArray: ByteArray): String {

        return try {

            val byte: Byte = byteArray[46]
            val int: Int = byte.toInt() and 0xFF
            println("The integer value is: $int")

            int.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    fun getCellCount(
        byteArray: ByteArray,
    ): Pair<String, BooleanArray> {

        return try {

            val bytes = byteArrayOf(byteArray[12], byteArray[13])
            val boolArray = BooleanArray(16)
            for (i in bytes.indices) {
                for (j in 0 until 8) {
                    boolArray[i * 8 + j] = bytes[i].toInt() and (1 shl j) != 0
                }
            }
            println("The Boolean array is: ${boolArray.contentToString()}")

            val trueCount = boolArray.count { it }
            println("The number of true values is: $trueCount")

            Pair(trueCount.toString(), boolArray)

        } catch (e: Exception) {
            Pair("-", BooleanArray(16))
        }
    }

    fun getCellVoltage(byteArray: ByteArray, start: Int, end: Int): String {

        return try {


            val bytes = byteArrayOf(
                byteArray[start], byteArray[end]
            )

            val uint16 = ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
            println("The uint16_t value is: $uint16")

            return uint16.toString()

        } catch (e: Exception) {
            "-"
        }
    }

}