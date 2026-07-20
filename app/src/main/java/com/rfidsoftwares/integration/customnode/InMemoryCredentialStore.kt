/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.integration.customnode

import com.rfidsoftwares.integration.models.Credential

interface CredentialStore {
    fun get(): Credential?
    fun set(credential: Credential)
    fun clear()
}

class InMemoryCredentialStore : CredentialStore {
    @Volatile
    private var credential: Credential? = null

    override fun get(): Credential? = credential

    override fun set(credential: Credential) {
        this.credential = credential
    }

    override fun clear() {
        credential = null
    }
}

