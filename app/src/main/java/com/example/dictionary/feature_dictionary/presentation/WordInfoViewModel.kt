package com.example.dictionary.feature_dictionary.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.core.util.Resource
import com.example.dictionary.feature_dictionary.domain.use_case.GetWordInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WordInfoViewModel @Inject constructor(
    private val getWordInfo: GetWordInfo
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _state = mutableStateOf(WordInfoState())
    val state: State<WordInfoState> = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    fun onSearch(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)

            getWordInfo(query)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            withContext(Dispatchers.Main) {
                                _state.value = state.value.copy(
                                    wordInfoItems = result.data ?: emptyList(),
                                    isLoading = false
                                )
                            }
                        }

                        is Resource.Error -> {
                            withContext(Dispatchers.Main) {
                                _state.value = state.value.copy(
                                    wordInfoItems = result.data ?: emptyList(),
                                    isLoading = false
                                )

                                _eventFlow.emit(
                                    UIEvent.ShowSnackBar(
                                        result.message ?: "Unknown error."
                                    )
                                )
                            }
                        }

                        is Resource.Loading -> {
                            withContext(Dispatchers.Main) {
                                _state.value = state.value.copy(
                                    wordInfoItems = result.data ?: emptyList(),
                                    isLoading = true
                                )
                            }
                        }
                    }
                }.launchIn(this)
        }
    }

    sealed class UIEvent {
        data class ShowSnackBar(val message: String) : UIEvent()
    }
}