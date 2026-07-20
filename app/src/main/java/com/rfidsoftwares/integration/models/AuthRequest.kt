/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.models

sealed class AuthRequest {
    data class DirectCredentialAuth(
        val username: String,
        val password: String,
    ) : AuthRequest()

    data class OAuthAuth(
        val code: String,
        val redirectUri: String,
    ) : AuthRequest()
}

