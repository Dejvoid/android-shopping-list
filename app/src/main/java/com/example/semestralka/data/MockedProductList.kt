package com.example.semestralka.data

import java.time.LocalDate

object MockedProductList : ProductListApi {
    private val list = listOf(
        ProductData(
            id = 1,
            identifier = "mouka",
            count = 1,
            expiry = LocalDate.now(),
        ),
        ProductData(
            id = 2,
            identifier = "mleko",
            count = 2,
            expiry = LocalDate.now(),
        ),
        ProductData(
            id = 3,
            identifier = "jogurt",
            count = 4,
            expiry = LocalDate.now(),
        )
    )
    override suspend fun getList(): Result<List<ProductData>> {
        return Result.success(
            list
        )
    }

    override suspend fun getByIdentifier(identifier: String): Result<ProductData> {
        val product = list.find { it.identifier == identifier }
        if (product != null)
            return Result.success(
                product
            )
        else
            return Result.failure(Throwable("Product not found"))
    }

    override suspend fun addProduct(product: ProductData): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateProduct(
        old: ProductData,
        new: ProductData
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteProduct(product: ProductData): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun exportShoppingToStorage(identifiers: List<String>): Result<Unit> {
        TODO("Not yet implemented")
    }
}