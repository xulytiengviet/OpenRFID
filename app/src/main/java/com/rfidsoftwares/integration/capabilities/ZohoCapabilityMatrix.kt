/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.capabilities

object ZohoCapabilityMatrix {
    val MATRIX: AdapterCapabilityMatrix = AdapterCapabilityMatrix(
        supportsOauth = false,
        supportsDirectAuth = false,
        supportsCustomerCreate = false,
        supportsEpcRegister = false,
        supportsCheckout = false,
        supportsAntiTheftFinalize = false,
        supportsDiagnosticsUpload = false,
    )
}

