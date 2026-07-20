/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.models

data class Credential(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochMs: Long?,
)

