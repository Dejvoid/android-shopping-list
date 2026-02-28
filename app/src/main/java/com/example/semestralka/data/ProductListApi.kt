package com.example.semestralka.data

interface ProductListApi {
    suspend fun getList() : Result<List<ProductData>>
    suspend fun getByIdentifier(identifier : String) : Result<ProductData>
    suspend fun addProduct(product : ProductData) : Result<Unit>
    suspend fun updateProduct(old : ProductData, new : ProductData) : Result<Unit>
    suspend fun deleteProduct(product: ProductData) : Result<Unit>
    // Maybe not needed in API?
    suspend fun exportShoppingToStorage(identifiers: List<String>) : Result<Unit>
}