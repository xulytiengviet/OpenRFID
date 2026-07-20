/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "AuditLog",
    indices = [
        Index(value = ["createdAt"], name = "idx_audit_created"),
        Index(value = ["providerConnectionId"], name = "idx_audit_provider"),
    ],
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val message: String,
    val detail: String?,
    val providerConnectionId: String?,
    val correlationId: String?,
    val createdAt: Long,
)
