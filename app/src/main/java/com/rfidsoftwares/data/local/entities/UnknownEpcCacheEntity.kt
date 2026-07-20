/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "UnknownEpcCache",
    primaryKeys = ["providerConnectionId", "epc"],
    indices = [
        Index(value = ["providerConnectionId"], name = "idx_unknown_provider"),
        Index(value = ["epc"], name = "idx_unknown_epc"),
        Index(value = ["lastSeenAt"], name = "idx_unknown_lastSeenAt"),
        Index(value = ["firstSeenAt"], name = "idx_unknown_firstSeenAt"),
    ]
)
data class UnknownEpcCacheEntity(
    val providerConnectionId: String,
    val epc: String,

    val firstSeenAt: Long,
    val lastSeenAt: Long,
    val seenCount: Int,
)

