/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.projections

data class InventoryProductStateRow(
    val productId: String,
    val productName: String,
    val sku: String?,
    val productStatus: String,
    val expectedCount: Int,
    val foundCount: Int,
    val sessionStatus: String,
)
