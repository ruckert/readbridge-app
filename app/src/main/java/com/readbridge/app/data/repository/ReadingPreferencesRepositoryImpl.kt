package com.readbridge.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.readbridge.app.domain.reader.ReadingPreferencesRepository
import com.readbridge.app.domain.reader.model.ContentWidth
import com.readbridge.app.domain.reader.model.ReaderFontFamily
import com.readbridge.app.domain.reader.model.ReaderFontWeight
import com.readbridge.app.domain.reader.model.ReaderTextAlign
import com.readbridge.app.domain.reader.model.ReadingPreferences
import com.readbridge.app.domain.reader.model.ReadingTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.readerDataStore: DataStore<Preferences> by preferencesDataStore(name = "reading_prefs")

@Singleton
class ReadingPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReadingPreferencesRepository {

    override val preferences: Flow<ReadingPreferences> =
        context.readerDataStore.data.map { it.toReadingPreferences() }

    override suspend fun update(preferences: ReadingPreferences) {
        context.readerDataStore.edit { prefs ->
            prefs[KEY_THEME] = preferences.theme.name
            prefs[KEY_FONT_FAMILY] = preferences.fontFamily.name
            prefs[KEY_FONT_SIZE] = preferences.fontSizeSp
            prefs[KEY_FONT_WEIGHT] = preferences.fontWeight.name
            prefs[KEY_LINE_HEIGHT] = preferences.lineHeight
            prefs[KEY_LETTER_SPACING] = preferences.letterSpacingEm
            prefs[KEY_CONTENT_WIDTH] = preferences.contentWidth.name
            prefs[KEY_TEXT_ALIGN] = preferences.textAlign.name
            prefs[KEY_HYPHENATION] = preferences.hyphenation
            prefs[KEY_CUSTOM_BG] = preferences.customBackgroundHex
            prefs[KEY_CUSTOM_TEXT] = preferences.customTextHex
        }
    }

    private fun Preferences.toReadingPreferences(): ReadingPreferences {
        val defaults = ReadingPreferences()
        return ReadingPreferences(
            theme = enumOrDefault(this[KEY_THEME], defaults.theme),
            fontFamily = enumOrDefault(this[KEY_FONT_FAMILY], defaults.fontFamily),
            fontSizeSp = this[KEY_FONT_SIZE] ?: defaults.fontSizeSp,
            fontWeight = enumOrDefault(this[KEY_FONT_WEIGHT], defaults.fontWeight),
            lineHeight = this[KEY_LINE_HEIGHT] ?: defaults.lineHeight,
            letterSpacingEm = this[KEY_LETTER_SPACING] ?: defaults.letterSpacingEm,
            contentWidth = enumOrDefault(this[KEY_CONTENT_WIDTH], defaults.contentWidth),
            textAlign = enumOrDefault(this[KEY_TEXT_ALIGN], defaults.textAlign),
            hyphenation = this[KEY_HYPHENATION] ?: defaults.hyphenation,
            customBackgroundHex = this[KEY_CUSTOM_BG] ?: defaults.customBackgroundHex,
            customTextHex = this[KEY_CUSTOM_TEXT] ?: defaults.customTextHex,
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        val KEY_FONT_SIZE = intPreferencesKey("font_size")
        val KEY_FONT_WEIGHT = stringPreferencesKey("font_weight")
        val KEY_LINE_HEIGHT = floatPreferencesKey("line_height")
        val KEY_LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val KEY_CONTENT_WIDTH = stringPreferencesKey("content_width")
        val KEY_TEXT_ALIGN = stringPreferencesKey("text_align")
        val KEY_HYPHENATION = booleanPreferencesKey("hyphenation")
        val KEY_CUSTOM_BG = stringPreferencesKey("custom_bg")
        val KEY_CUSTOM_TEXT = stringPreferencesKey("custom_text")
    }
}
