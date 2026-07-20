/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.dao

/**
 * Room mapping row for grouped outbox counts.
 */
data class OutboxStateCount(
    val state: String,
    val count: Int,
)
