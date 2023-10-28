package com.example.greenampbluetoothcontroller.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore


const val DEVICE_TABLE = "device_table"

val Context.deviceDataStore: DataStore<Preferences> by preferencesDataStore(name = DEVICE_TABLE)

suspend fun <T> DataStore<Preferences>.insertOrUpdate(key: Preferences.Key<T>, value: T) {
    edit { preferences ->
        preferences[key] = value
    }
}

