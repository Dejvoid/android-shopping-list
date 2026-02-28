package com.example.semestralka.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.semestralka.data.ProductData
import com.example.semestralka.data.ProductListApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(private val productList: ProductListApi) : ViewModel() {

    private val _state = MutableStateFlow<ListState>(ListState.Idle)
    val state: StateFlow<ListState> = _state

    private val _selectedIdentifiers = MutableStateFlow<Set<String>>(emptySet())
    val selectedIdentifiers: StateFlow<Set<String>> = _selectedIdentifiers

    // Track expanded cards in the ViewModel to survive rotation and navigation
    private val _expandedIdentifiers = MutableStateFlow<Set<String>>(emptySet())
    val expandedIdentifiers: StateFlow<Set<String>> = _expandedIdentifiers

    fun loadList() {
        viewModelScope.launch {
            _state.update { ListState.Loading }

            val result = productList.getList()
            result.onSuccess { products ->
                _state.update {
                    ListState.Loaded(products)
                }
            }.onFailure {
                _state.update {
                    ListState.Error
                }
            }
        }
    }

    fun toggleSelection(identifier: String) {
        _selectedIdentifiers.update { current ->
            if (current.contains(identifier)) current - identifier else current + identifier
        }
    }

    fun toggleExpanded(identifier: String) {
        _expandedIdentifiers.update { current ->
            if (current.contains(identifier)) current - identifier else current + identifier
        }
    }

    fun exportToStorage(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val identifiers = _selectedIdentifiers.value.toList()
            if (identifiers.isNotEmpty()) {
                productList.exportShoppingToStorage(identifiers).onSuccess {
                    _selectedIdentifiers.value = emptySet()
                    loadList()
                    onSuccess()
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: ProductListApi): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ListViewModel(repository) as T
            }
        }
    }
}

sealed interface ListState {
    data object Idle : ListState
    data object Loading : ListState
    data class Loaded(val products: List<ProductData>) : ListState
    data object Error : ListState
}