package com.example.semestralka.data.storage

import com.example.semestralka.data.ProductData
import com.example.semestralka.data.ProductDataDao
import com.example.semestralka.data.ProductListApi
import com.example.semestralka.data.ShoppingProductEntity
import com.example.semestralka.data.StorageProductEntity
import java.time.LocalDate

class LocalStorageProductList(
    private val dao: ProductDataDao,
    private val isShoppingList: Boolean
) : ProductListApi {

    override suspend fun getList(): Result<List<ProductData>> {
        return try {
            val products = if (isShoppingList) {
                dao.getShoppingAll().map { it.toProductData() }
            } else {
                dao.getStorageAll().map { it.toProductData() }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByIdentifier(identifier: String): Result<ProductData> {
        return try {
            val entity = if (isShoppingList) {
                dao.findShoppingByIdentifier(identifier)?.toProductData()
            } else {
                dao.findStorageByIdentifier(identifier)?.toProductData()
            }
            if (entity != null) Result.success(entity)
            else Result.failure(Exception("Not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProduct(product: ProductData): Result<Unit> {
        return try {
            if (isShoppingList) {
                dao.insertShopping(product.toShoppingEntity())
            } else {
                dao.insertStorage(product.toStorageEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(old: ProductData, new: ProductData): Result<Unit> {
        return try {
            if (isShoppingList) {
                dao.updateShopping(new.copy(id = old.id).toShoppingEntity())
            } else {
                dao.updateStorage(new.copy(id = old.id).toStorageEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: ProductData): Result<Unit> {
        return try {
            if (isShoppingList) {
                dao.deleteShopping(product.toShoppingEntity())
            } else {
                dao.deleteStorage(product.toStorageEntity())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportShoppingToStorage(identifiers: List<String>): Result<Unit> {
        return try {
            val shoppingItems = dao.getShoppingAll().filter { it.identifier in identifiers }
            shoppingItems.forEach { shoppingItem ->
                val existingStorageItem = dao.findStorageByIdentifier(shoppingItem.identifier)
                if (existingStorageItem != null) {
                    val newCount = existingStorageItem.count + shoppingItem.count
                    val newExpiry = minDate(existingStorageItem.expiry, shoppingItem.expiry)
                    
                    dao.updateStorage(existingStorageItem.copy(
                        count = newCount,
                        expiry = newExpiry
                    ))
                } else {
                    dao.insertStorage(StorageProductEntity(
                        id = 0,
                        identifier = shoppingItem.identifier,
                        count = shoppingItem.count,
                        expiry = shoppingItem.expiry
                    ))
                }
                dao.deleteShoppingByIdentifier(shoppingItem.identifier)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun minDate(d1: LocalDate?, d2: LocalDate?): LocalDate? {
        return when {
            d1 == null -> d2
            d2 == null -> d1
            d1.isBefore(d2) -> d1
            else -> d2
        }
    }

    private fun StorageProductEntity.toProductData() = ProductData(id, identifier, count, expiry)
    private fun ShoppingProductEntity.toProductData() = ProductData(id, identifier, count, expiry)
    private fun ProductData.toStorageEntity() = StorageProductEntity(id, identifier, count, expiry)
    private fun ProductData.toShoppingEntity() = ShoppingProductEntity(id, identifier, count, expiry)
}