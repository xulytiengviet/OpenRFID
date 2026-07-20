/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "SessionScan",
    primaryKeys = ["sessionId", "providerConnectionId", "epc"],
    indices = [
        Index(value = ["providerConnectionId"], name = "idx_scan_provider"),
        Index(value = ["sessionId"], name = "idx_scan_sessionId"),
        Index(value = ["epc"], name = "idx_scan_epc"),
        Index(value = ["productId"], name = "idx_scan_productId"),
        Index(value = ["firstSeenAt"], name = "idx_scan_firstSeenAt"),
    ]
)
data class SessionScanEntity(
    val sessionId: String,
    val providerConnectionId: String,
    val epc: String,

    val firstSeenAt: Long,
    val source: String,

    // Stock calculations are derived from expected snapshot.
    val isKnown: Boolean,
    val productId: String?,
)

