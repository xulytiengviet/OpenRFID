/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.models

/**
 * Canonical catalog snapshot used by inventory sync:
 * - products describe the expected catalog rows
 * - product EPC mappings describe expected EPC->product relationships
 */
data class ProductCatalogSnapshot(
    val products: List<Product>,
    val productEpcs: List<ProductEpc>,
)

