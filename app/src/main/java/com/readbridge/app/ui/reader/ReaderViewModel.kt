package com.readbridge.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readbridge.app.domain.reader.ReaderRepository
import com.readbridge.app.domain.reader.ReadingPreferencesRepository
import com.readbridge.app.domain.reader.model.ReaderArticle
import com.readbridge.app.domain.reader.model.ReadingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readerRepository: ReaderRepository,
    private val preferencesRepository: ReadingPreferencesRepository,
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>(ARG_ENTRY_ID) ?: -1L

    val article: StateFlow<ReaderArticle?> =
        readerRepository.observeArticle(entryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preferences: StateFlow<ReadingPreferences> =
        preferencesRepository.preferences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReadingPreferences())

    /** Null until the saved scroll position has been loaded (gates the WebView so restore works). */
    private val _initialScrollRatio = MutableStateFlow<Float?>(null)
    val initialScrollRatio: StateFlow<Float?> = _initialScrollRatio.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _initialScrollRatio.value = readerRepository.getProgress(entryId)
        }
    }

    fun updatePreferences(preferences: ReadingPreferences) {
        viewModelScope.launch { preferencesRepository.update(preferences) }
    }

    fun toggleStar() {
        val current = article.value ?: return
        viewModelScope.launch {
            val ok = readerRepository.setStarred(entryId, !current.isStarred)
            if (!ok) _message.value = "Não foi possível atualizar o favorito (sem conexão)."
        }
    }

    fun toggleArchive() {
        val current = article.value ?: return
        viewModelScope.launch {
            val ok = readerRepository.setArchived(entryId, !current.isArchived)
            if (!ok) _message.value = "Não foi possível arquivar (sem conexão)."
        }
    }

    fun saveProgress(ratio: Float) {
        viewModelScope.launch { readerRepository.saveProgress(entryId, ratio) }
    }

    fun consumeMessage() {
        _message.value = null
    }

    private companion object {
        const val ARG_ENTRY_ID = "entryId"
    }
}
