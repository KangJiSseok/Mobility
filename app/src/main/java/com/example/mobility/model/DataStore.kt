package com.example.mobility.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreKey.DATA_STORE_NAME)

object DataStoreKey {
    const val DATA_STORE_NAME = "settings"
    val IS_NOTIFICATION_ON = booleanPreferencesKey("is_notification_on")
}