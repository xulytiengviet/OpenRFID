/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.models

data class ProductEpc(
    val epc: String,
    val providerConnectionId: String,

    val productId: String,
    val state: String,

    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,

    // Optional provider meta preserved for adapter-layer canonical mapping.
    // Note: local Room entity does not persist this yet (future schema expansion).
    val meta: String? = null,
)

