package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "salim_settings")

data class UserSettings(
    val isTrueBlackOled: Boolean = true,
    val themeMode: String = "dark",
    val liquidGlassAlpha: Float = 0.65f,
    val highContrastControls: Boolean = false,
    val defaultSkipDurationSec: Int = 10,
    val defaultPlaybackSpeed: Float = 1.0f,
    val subtitleFontSizeSp: Int = 18,
    val subtitleTextColorHex: String = "#FFFFFF",
    val subtitleBackgroundEnabled: Boolean = true,
    val aspectRatioMode: String = "fit",
    val hasCompletedOnboarding: Boolean = false,
    val viewMode: String = "grid",
    val sortOption: String = "date",
    val excludedFolders: Set<String> = emptySet()
)

class UserSettingsRepository(private val context: Context) {

    private object Keys {
        val TRUE_BLACK_OLED = booleanPreferencesKey("true_black_oled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LIQUID_GLASS_ALPHA = floatPreferencesKey("liquid_glass_alpha")
        val HIGH_CONTRAST_CONTROLS = booleanPreferencesKey("high_contrast_controls")
        val DEFAULT_SKIP_DURATION = intPreferencesKey("default_skip_duration")
        val DEFAULT_PLAYBACK_SPEED = floatPreferencesKey("default_playback_speed")
        val SUBTITLE_FONT_SIZE = intPreferencesKey("subtitle_font_size")
        val SUBTITLE_TEXT_COLOR = stringPreferencesKey("subtitle_text_color")
        val SUBTITLE_BG_ENABLED = booleanPreferencesKey("subtitle_bg_enabled")
        val ASPECT_RATIO_MODE = stringPreferencesKey("aspect_ratio_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            isTrueBlackOled = prefs[Keys.TRUE_BLACK_OLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE] ?: "dark",
            liquidGlassAlpha = prefs[Keys.LIQUID_GLASS_ALPHA] ?: 0.65f,
            highContrastControls = prefs[Keys.HIGH_CONTRAST_CONTROLS] ?: false,
            defaultSkipDurationSec = prefs[Keys.DEFAULT_SKIP_DURATION] ?: 10,
            defaultPlaybackSpeed = prefs[Keys.DEFAULT_PLAYBACK_SPEED] ?: 1.0f,
            subtitleFontSizeSp = prefs[Keys.SUBTITLE_FONT_SIZE] ?: 18,
            subtitleTextColorHex = prefs[Keys.SUBTITLE_TEXT_COLOR] ?: "#FFFFFF",
            subtitleBackgroundEnabled = prefs[Keys.SUBTITLE_BG_ENABLED] ?: true,
            aspectRatioMode = prefs[Keys.ASPECT_RATIO_MODE] ?: "fit",
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            viewMode = prefs[Keys.VIEW_MODE] ?: "grid",
            sortOption = prefs[Keys.SORT_OPTION] ?: "date",
            excludedFolders = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
        )
    }

    suspend fun setTrueBlackOled(enabled: Boolean) = edit { it[Keys.TRUE_BLACK_OLED] = enabled }
    suspend fun setThemeMode(mode: String) = edit { it[Keys.THEME_MODE] = mode }
    suspend fun setLiquidGlassAlpha(alpha: Float) = edit { it[Keys.LIQUID_GLASS_ALPHA] = alpha.coerceIn(0.2f, 1.0f) }
    suspend fun setHighContrastControls(enabled: Boolean) = edit { it[Keys.HIGH_CONTRAST_CONTROLS] = enabled }
    suspend fun setDefaultSkipDuration(sec: Int) = edit { it[Keys.DEFAULT_SKIP_DURATION] = sec }
    suspend fun setDefaultPlaybackSpeed(speed: Float) = edit { it[Keys.DEFAULT_PLAYBACK_SPEED] = speed }
    suspend fun setSubtitleFontSize(size: Int) = edit { it[Keys.SUBTITLE_FONT_SIZE] = size }
    suspend fun setSubtitleTextColor(colorHex: String) = edit { it[Keys.SUBTITLE_TEXT_COLOR] = colorHex }
    suspend fun setSubtitleBackgroundEnabled(enabled: Boolean) = edit { it[Keys.SUBTITLE_BG_ENABLED] = enabled }
    suspend fun setAspectRatioMode(mode: String) = edit { it[Keys.ASPECT_RATIO_MODE] = mode }
    suspend fun setOnboardingCompleted(completed: Boolean) = edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    suspend fun setViewMode(mode: String) = edit { it[Keys.VIEW_MODE] = mode }
    suspend fun setSortOption(sort: String) = edit { it[Keys.SORT_OPTION] = sort }

    suspend fun addExcludedFolder(folder: String) = edit { prefs ->
        val current = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
        prefs[Keys.EXCLUDED_FOLDERS] = current + folder
    }

    suspend fun removeExcludedFolder(folder: String) = edit { prefs ->
        val current = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
        prefs[Keys.EXCLUDED_FOLDERS] = current - folder
    }

    private suspend fun edit(action: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(action)
    }
}
