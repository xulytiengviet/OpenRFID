/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.ui.screens

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.rfidsoftwares.R
import com.rfidsoftwares.common.prefs.UhfConfigPrefs
import com.rfidsoftwares.rfid.driver.RfidDriverFactory
import com.rfidsoftwares.ui.base.BaseScreenFragment

class ModeSelectorFragment : BaseScreenFragment() {

    override fun screenTitle(): String = "OpenRFID Suite"
    override fun screenSubtitle(): String? = "Choose an operation mode to start"
    override fun isTopLevelScreen(): Boolean = true
    override fun allowOfflinePanel(): Boolean = false

    override fun createBody(inflater: LayoutInflater, container: FrameLayout): View {
        return inflater.inflate(R.layout.body_mode_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activeDriverText: TextView = view.findViewById(R.id.hardwareActiveDriverText)
        val deviceInfoText: TextView = view.findViewById(R.id.hardwareDeviceInfoText)
        val changeDriverBtn: MaterialButton = view.findViewById(R.id.btnChangeHardwareDriver)

        // Populate Active Hardware Status
        val configuredDriverId = UhfConfigPrefs.load(requireContext()).driverId ?: ""
        val driverInstance = RfidDriverFactory.createDriver(configuredDriverId)
        val driverName = driverInstance.displayName

        if (configuredDriverId.equals("auto", ignoreCase = true) || configuredDriverId.isBlank()) {
            activeDriverText.text = "Driver: $driverName (Auto-Detected)"
        } else {
            activeDriverText.text = "Driver: $driverName (Manual Override)"
        }

        deviceInfoText.text = "Device: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

        changeDriverBtn.setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_uhfConfigFragment)
        }

        // Quick Testing Tools (No Login Required)
        view.findViewById<MaterialCardView>(R.id.btnQuickReadTag).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_readTagFragment)
        }

        view.findViewById<MaterialCardView>(R.id.btnQuickWriteTag).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_writeTagFragment)
        }

        view.findViewById<MaterialCardView>(R.id.btnQuickUpdateEpc).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_updateEpcFragment)
        }

        view.findViewById<MaterialCardView>(R.id.btnQuickTagLock).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_tagLockFragment)
        }

        view.findViewById<MaterialCardView>(R.id.btnQuickUhfConfig).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_uhfConfigFragment)
        }

        view.findViewById<MaterialCardView>(R.id.btnQuickDiagnostics).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_diagnosticsFragment)
        }

        // Enterprise Inventory Suite (Login & Sync)
        view.findViewById<MaterialCardView>(R.id.btnEnterpriseInventory).setOnClickListener {
            findNavController().navigate(R.id.action_modeSelector_to_firstOpenCheckFragment)
        }
    }
}
