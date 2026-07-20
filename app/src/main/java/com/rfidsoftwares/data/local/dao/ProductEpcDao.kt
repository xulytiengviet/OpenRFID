/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rfidsoftwares.data.local.entities.ProductEpcEntity

@Dao
interface ProductEpcDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertEpcs(epcs: List<ProductEpcEntity>)

    @Query("SELECT * FROM ProductEpc WHERE providerConnectionId = :providerConnectionId")
    fun getProductEpcs(providerConnectionId: String): List<ProductEpcEntity>

    @Query(
        "SELECT * FROM ProductEpc " +
            "WHERE providerConnectionId = :providerConnectionId AND productId = :productId"
    )
    fun getProductEpcsForProduct(providerConnectionId: String, productId: String): List<ProductEpcEntity>

    @Query("DELETE FROM ProductEpc WHERE providerConnectionId = :providerConnectionId")
    fun deleteByProvider(providerConnectionId: String)
}

