package com.fitpulse.app.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val accessToken = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
        val refreshToken = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
        val userId = stringPreferencesKey(Constants.KEY_USER_ID)
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[Keys.accessToken] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[Keys.refreshToken] }

    suspend fun saveTokens(access: String, refresh: String, userId: String) {
        context.dataStore.edit {
            it[Keys.accessToken] = access
            it[Keys.refreshToken] = refresh
            it[Keys.userId] = userId
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
