/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.common.auth

/**
 * Phase 3 UI wiring helper.
 *
 * Stores the currently authenticated provider connection id so subsequent screens
 * (inventory sync, push, etc.) operate on the correct provider namespace.
 */
object ActiveProviderStore {
    @Volatile
    var activeProviderId: String? = null
}

