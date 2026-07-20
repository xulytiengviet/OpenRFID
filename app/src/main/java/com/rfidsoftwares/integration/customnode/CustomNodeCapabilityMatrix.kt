/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.customnode

import com.rfidsoftwares.integration.capabilities.AdapterCapabilityMatrix

object CustomNodeCapabilityMatrix {
    val MATRIX: AdapterCapabilityMatrix = AdapterCapabilityMatrix(
        supportsOauth = true,
        supportsDirectAuth = true,
        supportsCustomerCreate = true,
        supportsEpcRegister = true,
        supportsCheckout = true,
        supportsAntiTheftFinalize = true,
        supportsDiagnosticsUpload = true,
    )
}

