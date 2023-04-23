package com.meriniguan.kpdplus.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_DATE_CREATED, BY_NAME }

enum class ShowSetting { ALL, FREE, HELD }

class FilterPreferences(val sortOrder: SortOrder, val showSetting: ShowSetting)

class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val Context._dataStore by preferencesDataStore("user_preferences")

    private val dataStore : DataStore<Preferences> = context._dataStore

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE_CREATED.name
            )
            val showSetting = ShowSetting.valueOf(
                preferences[PreferencesKeys.SHOW_SETTING] ?: ShowSetting.ALL.name
            )
            FilterPreferences(sortOrder, showSetting)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateShowSetting(showSetting: ShowSetting) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SETTING] = showSetting.name
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOW_SETTING = stringPreferencesKey("show_setting")
    }
}