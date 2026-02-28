package com.example.semestralka.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.semestralka.data.ProductData
import com.example.semestralka.data.ProductListApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DetailViewModel(private val storage: ProductListApi) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    private var originalProduct: ProductData? = null

    fun loadProduct(identifier: String?) {
        if (identifier == null) return
        viewModelScope.launch {
            storage.getByIdentifier(identifier).onSuccess { product ->
                originalProduct = product
                _state.update {
                    it.copy(
                        identifier = product.identifier,
                        count = product.count,
                        expiry = product.expiry
                    )
                }
            }
        }
    }

    fun updateIdentifier(identifier: String) {
        _state.update { it.copy(identifier = identifier) }
    }

    fun updateCount(count: Int) {
        _state.update { it.copy(count = count) }
    }

    fun updateExpiry(expiry: LocalDate?) {
        _state.update { it.copy(expiry = expiry) }
    }

    fun saveProduct(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            if (originalProduct != null) {
                // Update existing product
                val updatedProduct = originalProduct!!.copy(
                    identifier = currentState.identifier,
                    count = currentState.count,
                    expiry = currentState.expiry
                )
                storage.updateProduct(originalProduct!!, updatedProduct).onSuccess {
                    onSuccess()
                }
            } else {
                // Add new product
                val newProduct = ProductData(
                    id = 0,
                    identifier = currentState.identifier,
                    count = currentState.count,
                    expiry = currentState.expiry
                )
                storage.addProduct(newProduct).onSuccess {
                    onSuccess()
                }
            }
        }
    }

    fun deleteProduct(onSuccess: () -> Unit) {
        viewModelScope.launch {
            originalProduct?.let { product ->
                storage.deleteProduct(product).onSuccess {
                    onSuccess()
                }
            }
        }
    }

    companion object {
        fun provideFactory(storage: ProductListApi): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(storage) as T
            }
        }
    }
}

data class DetailState(
    val identifier: String = "",
    val count: Int = 0,
    val expiry: LocalDate? = null
)