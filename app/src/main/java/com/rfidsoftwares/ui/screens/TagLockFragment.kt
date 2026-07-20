/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.rfidsoftwares.R
import com.rfidsoftwares.common.prefs.UhfConfigPrefs
import com.rfidsoftwares.rfid.driver.RfidDriverFactory
import com.rfidsoftwares.rfid.driver.RfidReaderDriver
import com.rfidsoftwares.ui.base.BaseScreenFragment
import java.util.concurrent.Executors

class TagLockFragment : BaseScreenFragment() {

    override fun screenTitle(): String = "Tag Security & Locking"
    override fun screenSubtitle(): String? = "Lock or unlock tag memory access permissions"
    override fun allowOfflinePanel(): Boolean = false

    private var bg = Executors.newSingleThreadExecutor()
    private var driver: RfidReaderDriver? = null

    override fun createBody(inflater: LayoutInflater, container: FrameLayout): View {
        return inflater.inflate(R.layout.body_tag_lock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureBg()

        val epcInput: TextInputEditText = view.findViewById(R.id.tagLockEpcInput)
        val pwdInput: TextInputEditText = view.findViewById(R.id.tagLockAccessPwdInput)
        val statusText: TextView = view.findViewById(R.id.tagLockStatusText)
        val lockBtn: MaterialButton = view.findViewById(R.id.tagLockButton)
        val unlockBtn: MaterialButton = view.findViewById(R.id.tagUnlockButton)

        lockBtn.setOnClickListener {
            statusText.text = "Sending lock command to tag..."
            val epc = epcInput.text?.toString()?.trim()
            val pwd = pwdInput.text?.toString()?.trim()
            bg.execute {
                val msg = runCatching {
                    val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                    val initOk = d.init(requireContext().applicationContext)
                    if (!initOk) return@runCatching "⚠️ Reader Init Failed (${d.displayName}): Could not connect to physical RFID hardware module. Ensure reader power is ON."
                    val ok = d.setLockState(epc = epc, accessPasswordHex = pwd, lock = true)
                    if (ok) "Lock command executed successfully." else "Lock command failed (check access password or tag proximity)."
                }.getOrElse {
                    "Lock failed: ${it.message ?: it.javaClass.simpleName}"
                }
                view.post { statusText.text = msg }
            }
        }

        unlockBtn.setOnClickListener {
            statusText.text = "Sending unlock command to tag..."
            val epc = epcInput.text?.toString()?.trim()
            val pwd = pwdInput.text?.toString()?.trim()
            bg.execute {
                val msg = runCatching {
                    val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                    val initOk = d.init(requireContext().applicationContext)
                    if (!initOk) return@runCatching "⚠️ Reader Init Failed (${d.displayName}): Could not connect to physical RFID hardware module. Ensure reader power is ON."
                    val ok = d.setLockState(epc = epc, accessPasswordHex = pwd, lock = false)
                    if (ok) "Unlock command executed successfully." else "Unlock command failed (check access password or tag proximity)."
                }.getOrElse {
                    "Unlock failed: ${it.message ?: it.javaClass.simpleName}"
                }
                view.post { statusText.text = msg }
            }
        }
    }

    override fun onDestroyView() {
        try {
            driver?.release()
        } catch (_: Exception) {
        }
        driver = null
        bg.shutdownNow()
        super.onDestroyView()
    }

    private fun ensureBg() {
        if (bg.isShutdown || bg.isTerminated) {
            bg = Executors.newSingleThreadExecutor()
        }
    }
}
