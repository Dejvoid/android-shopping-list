package com.example.semestralka.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductDataDao {
    // Storage methods
    @Query("SELECT * FROM storage_products")
    suspend fun getStorageAll(): List<StorageProductEntity>

    @Query("SELECT * FROM storage_products WHERE identifier = :identifier LIMIT 1")
    suspend fun findStorageByIdentifier(identifier: String): StorageProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorage(product: StorageProductEntity)

    @Update
    suspend fun updateStorage(product: StorageProductEntity)

    @Delete
    suspend fun deleteStorage(product: StorageProductEntity)

    // Shopping methods
    @Query("SELECT * FROM shopping_products")
    suspend fun getShoppingAll(): List<ShoppingProductEntity>

    @Query("SELECT * FROM shopping_products WHERE identifier = :identifier LIMIT 1")
    suspend fun findShoppingByIdentifier(identifier: String): ShoppingProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopping(product: ShoppingProductEntity)

    @Update
    suspend fun updateShopping(product: ShoppingProductEntity)

    @Delete
    suspend fun deleteShopping(product: ShoppingProductEntity)

    @Query("DELETE FROM shopping_products WHERE identifier = :identifier")
    suspend fun deleteShoppingByIdentifier(identifier: String)

    @Query("DELETE FROM shopping_products")
    suspend fun deleteAllShopping()
}