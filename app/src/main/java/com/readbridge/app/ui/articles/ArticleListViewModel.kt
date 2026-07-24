package com.readbridge.app.ui.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.readbridge.app.domain.article.model.Article
import com.readbridge.app.domain.article.model.ArticleFilter
import com.readbridge.app.domain.article.model.SyncResult
import com.readbridge.app.domain.article.usecase.ObserveArticlesUseCase
import com.readbridge.app.domain.article.usecase.SyncArticlesUseCase
import com.readbridge.app.domain.auth.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticleListViewModel @Inject constructor(
    observeArticles: ObserveArticlesUseCase,
    private val syncArticles: SyncArticlesUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(ArticleFilter.Unread)
    val filter: StateFlow<ArticleFilter> = _filter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val articles: Flow<PagingData<Article>> =
        _filter.flatMapLatest { observeArticles(it) }.cachedIn(viewModelScope)

    init {
        // First launch syncs everything (no cursor yet); later runs are incremental.
        refresh()
    }

    fun setFilter(filter: ArticleFilter) {
        _filter.value = filter
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = syncArticles(fullRefresh = false)) {
                is SyncResult.Error -> _message.value = result.message
                is SyncResult.Success -> Unit
            }
            _isRefreshing.value = false
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun onLogout() {
        viewModelScope.launch { logout() }
    }
}
