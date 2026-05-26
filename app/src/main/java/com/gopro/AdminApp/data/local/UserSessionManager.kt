package com.gopro.AdminApp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {
    private val gson = Gson()

    private val EMAIL = stringPreferencesKey("email")
    private val FULL_NAME = stringPreferencesKey("full_name")
    private val ROLES = stringPreferencesKey("roles")
    private val PERMISSIONS = stringPreferencesKey("permissions")

    private val PHOTO_URL = stringPreferencesKey("photo_url")

    suspend fun saveSession(email: String, name: String, roles: List<String>, perms: List<String>,photoUrl: String?) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL] = email
            prefs[FULL_NAME] = name
            prefs[ROLES] = gson.toJson(roles)
            prefs[PERMISSIONS] = gson.toJson(perms)
            prefs[PHOTO_URL] = photoUrl ?: ""
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FULL_NAME] ?: "Guest"
    }

    val emailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EMAIL] ?: ""
    }

    val rolesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[ROLES] ?: "[]"
        gson.fromJson(json, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    }

    val permissionsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[PERMISSIONS] ?: "[]"
        gson.fromJson(json, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    }

    val userPhotoFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        val photo = prefs[PHOTO_URL]
        if (photo.isNullOrEmpty()) null else photo
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}