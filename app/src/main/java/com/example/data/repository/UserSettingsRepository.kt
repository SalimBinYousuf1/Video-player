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
    val isTrueBlackOled: Boolean = false,
    val themeMode: String = "light",
    val smokeGradientTheme: String = "blue_green_orange",
    val smokeIntensity: Float = 0.45f,
    val smokeSpeed: Float = 1.0f,
    val liquidGlassAlpha: Float = 0.65f,
    val highContrastControls: Boolean = false,
    val defaultSkipDurationSec: Int = 20,
    val defaultPlaybackSpeed: Float = 1.0f,
    val audioBoostEnabled: Boolean = false,
    val longPressFastForward: Boolean = true,
    val screenOrientationLock: String = "sensor",
    val subtitleFontSizeSp: Int = 18,
    val subtitleTextColorHex: String = "#FFFFFF",
    val subtitleBackgroundEnabled: Boolean = true,
    val subtitleBackgroundStyle: String = "pill",
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
        val SMOKE_GRADIENT_THEME = stringPreferencesKey("smoke_gradient_theme")
        val SMOKE_INTENSITY = floatPreferencesKey("smoke_intensity")
        val SMOKE_SPEED = floatPreferencesKey("smoke_speed")
        val LIQUID_GLASS_ALPHA = floatPreferencesKey("liquid_glass_alpha")
        val HIGH_CONTRAST_CONTROLS = booleanPreferencesKey("high_contrast_controls")
        val DEFAULT_SKIP_DURATION = intPreferencesKey("default_skip_duration")
        val DEFAULT_PLAYBACK_SPEED = floatPreferencesKey("default_playback_speed")
        val AUDIO_BOOST_ENABLED = booleanPreferencesKey("audio_boost_enabled")
        val LONG_PRESS_FAST_FORWARD = booleanPreferencesKey("long_press_fast_forward")
        val SCREEN_ORIENTATION_LOCK = stringPreferencesKey("screen_orientation_lock")
        val SUBTITLE_FONT_SIZE = intPreferencesKey("subtitle_font_size")
        val SUBTITLE_TEXT_COLOR = stringPreferencesKey("subtitle_text_color")
        val SUBTITLE_BG_ENABLED = booleanPreferencesKey("subtitle_bg_enabled")
        val SUBTITLE_BG_STYLE = stringPreferencesKey("subtitle_bg_style")
        val ASPECT_RATIO_MODE = stringPreferencesKey("aspect_ratio_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            isTrueBlackOled = prefs[Keys.TRUE_BLACK_OLED] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "light",
            smokeGradientTheme = prefs[Keys.SMOKE_GRADIENT_THEME] ?: "blue_green_orange",
            smokeIntensity = prefs[Keys.SMOKE_INTENSITY] ?: 0.45f,
            smokeSpeed = prefs[Keys.SMOKE_SPEED] ?: 1.0f,
            liquidGlassAlpha = prefs[Keys.LIQUID_GLASS_ALPHA] ?: 0.65f,
            highContrastControls = prefs[Keys.HIGH_CONTRAST_CONTROLS] ?: false,
            defaultSkipDurationSec = prefs[Keys.DEFAULT_SKIP_DURATION] ?: 20,
            defaultPlaybackSpeed = prefs[Keys.DEFAULT_PLAYBACK_SPEED] ?: 1.0f,
            audioBoostEnabled = prefs[Keys.AUDIO_BOOST_ENABLED] ?: false,
            longPressFastForward = prefs[Keys.LONG_PRESS_FAST_FORWARD] ?: true,
            screenOrientationLock = prefs[Keys.SCREEN_ORIENTATION_LOCK] ?: "sensor",
            subtitleFontSizeSp = prefs[Keys.SUBTITLE_FONT_SIZE] ?: 18,
            subtitleTextColorHex = prefs[Keys.SUBTITLE_TEXT_COLOR] ?: "#FFFFFF",
            subtitleBackgroundEnabled = prefs[Keys.SUBTITLE_BG_ENABLED] ?: true,
            subtitleBackgroundStyle = prefs[Keys.SUBTITLE_BG_STYLE] ?: "pill",
            aspectRatioMode = prefs[Keys.ASPECT_RATIO_MODE] ?: "fit",
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            viewMode = prefs[Keys.VIEW_MODE] ?: "grid",
            sortOption = prefs[Keys.SORT_OPTION] ?: "date",
            excludedFolders = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
        )
    }

    suspend fun setTrueBlackOled(enabled: Boolean) = edit { it[Keys.TRUE_BLACK_OLED] = enabled }
    suspend fun setThemeMode(mode: String) = edit { it[Keys.THEME_MODE] = mode }
    suspend fun setSmokeGradientTheme(theme: String) = edit { it[Keys.SMOKE_GRADIENT_THEME] = theme }
    suspend fun setSmokeIntensity(intensity: Float) = edit { it[Keys.SMOKE_INTENSITY] = intensity }
    suspend fun setSmokeSpeed(speed: Float) = edit { it[Keys.SMOKE_SPEED] = speed }
    suspend fun setLiquidGlassAlpha(alpha: Float) = edit { it[Keys.LIQUID_GLASS_ALPHA] = alpha.coerceIn(0.2f, 1.0f) }
    suspend fun setHighContrastControls(enabled: Boolean) = edit { it[Keys.HIGH_CONTRAST_CONTROLS] = enabled }
    suspend fun setDefaultSkipDuration(sec: Int) = edit { it[Keys.DEFAULT_SKIP_DURATION] = sec }
    suspend fun setDefaultPlaybackSpeed(speed: Float) = edit { it[Keys.DEFAULT_PLAYBACK_SPEED] = speed }
    suspend fun setAudioBoostEnabled(enabled: Boolean) = edit { it[Keys.AUDIO_BOOST_ENABLED] = enabled }
    suspend fun setLongPressFastForward(enabled: Boolean) = edit { it[Keys.LONG_PRESS_FAST_FORWARD] = enabled }
    suspend fun setScreenOrientationLock(lock: String) = edit { it[Keys.SCREEN_ORIENTATION_LOCK] = lock }
    suspend fun setSubtitleFontSize(size: Int) = edit { it[Keys.SUBTITLE_FONT_SIZE] = size }
    suspend fun setSubtitleTextColor(colorHex: String) = edit { it[Keys.SUBTITLE_TEXT_COLOR] = colorHex }
    suspend fun setSubtitleBackgroundEnabled(enabled: Boolean) = edit { it[Keys.SUBTITLE_BG_ENABLED] = enabled }
    suspend fun setSubtitleBackgroundStyle(style: String) = edit { it[Keys.SUBTITLE_BG_STYLE] = style }
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
