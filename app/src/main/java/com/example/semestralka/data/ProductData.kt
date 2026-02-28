package com.example.semestralka.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate

// Plain data class for UI and API
data class ProductData (
    val id: Int,
    val identifier: String,
    val count: Int,
    val expiry: LocalDate?,
)

@Entity(tableName = "storage_products")
data class StorageProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val identifier: String,
    val count: Int,
    val expiry: LocalDate?,
)

@Entity(tableName = "shopping_products")
data class ShoppingProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val identifier: String,
    val count: Int,
    val expiry: LocalDate?,
)

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?) : LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}