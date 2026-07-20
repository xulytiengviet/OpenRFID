/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.capabilities

data class AdapterCapabilityMatrix(
    val supportsOauth: Boolean,
    val supportsDirectAuth: Boolean,
    val supportsCustomerCreate: Boolean,
    val supportsEpcRegister: Boolean,
    val supportsCheckout: Boolean,
    val supportsAntiTheftFinalize: Boolean,
    val supportsDiagnosticsUpload: Boolean,
)

