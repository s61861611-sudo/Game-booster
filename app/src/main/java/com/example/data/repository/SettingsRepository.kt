package com.example.data.repository

import com.example.data.local.SettingsDao
import com.example.data.model.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    val allSettings: Flow<List<SettingsEntity>> = settingsDao.getAllSettingsFlow()

    suspend fun getSetting(key: String, defaultValue: String): String {
        return settingsDao.getSettingByKey(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        settingsDao.saveSetting(SettingsEntity(key, value))
    }
}
