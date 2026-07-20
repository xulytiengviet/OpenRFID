/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.rfid

data class TagEvent(
    val epc: String,
    val rssi: Int?,
    val source: TagSource,
    val seenAt: Long,
)

enum class TagSource {
    EPC,
    TID,
    USER,
}

