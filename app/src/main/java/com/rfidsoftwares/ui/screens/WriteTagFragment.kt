/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.rfidsoftwares.R
import com.rfidsoftwares.common.prefs.UhfConfigPrefs
import com.rfidsoftwares.common.sound.SoundBeeper
import com.rfidsoftwares.rfid.driver.MemoryBank
import com.rfidsoftwares.rfid.driver.RfidDriverFactory
import com.rfidsoftwares.rfid.driver.RfidReaderDriver
import com.rfidsoftwares.ui.base.BaseScreenFragment
import java.util.concurrent.Executors

class WriteTagFragment : BaseScreenFragment() {
    override fun screenTitle(): String = "Write Tag"
    override fun screenSubtitle(): String? = "Write EPC/USER memory and set Access password"
    override fun allowOfflinePanel(): Boolean = false

    private var bg = Executors.newSingleThreadExecutor()
    private var driver: RfidReaderDriver? = null
    private var isDriverReady = false

    override fun createBody(inflater: LayoutInflater, container: FrameLayout): View {
        return inflater.inflate(R.layout.body_write_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureBg()

        val epcInput: TextInputEditText = view.findViewById(R.id.writeTagEpcInput)
        val pwdInput: TextInputEditText = view.findViewById(R.id.writeTagAccessPwdInput)
        val bankInput: TextInputEditText = view.findViewById(R.id.writeTagBankInput)
        val ptrInput: TextInputEditText = view.findViewById(R.id.writeTagWordPtrInput)
        val dataInput: TextInputEditText = view.findViewById(R.id.writeTagDataInput)
        val newPwdInput: TextInputEditText = view.findViewById(R.id.writeTagNewAccessPwdInput)
        val statusText: TextView = view.findViewById(R.id.writeTagStatusText)

        val scanBtn: MaterialButton = view.findViewById(R.id.writeTagScanButton)
        val writeBtn: MaterialButton = view.findViewById(R.id.writeTagWriteButton)
        val setPwdBtn: MaterialButton = view.findViewById(R.id.writeTagSetPwdButton)
        val lockBtn: MaterialButton = view.findViewById(R.id.writeTagLockButton)
        val unlockBtn: MaterialButton = view.findViewById(R.id.writeTagUnlockButton)

        bankInput.setText("USER")
        ptrInput.setText("0")
        statusText.text = "Initializing reader hardware..."

        // Eager Hardware Reader Initialization on Fragment Entry
        bg.execute {
            val d = RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId)
            driver = d
            val ok = try {
                d.init(requireContext().applicationContext)
            } catch (e: Exception) {
                false
            }
            isDriverReady = ok
            view.post {
                if (ok) {
                    statusText.text = "Reader ready (${d.displayName}). Enter data (HEX) and tap operation."
                } else {
                    statusText.text = "⚠️ Hardware Init Failed (${d.displayName}): Could not connect to RFID module."
                }
            }
        }

        scanBtn.setOnClickListener {
            statusText.text = "Scanning nearest tag..."
            bg.execute {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    view.post { statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Check hardware connection." }
                    return@execute
                }

                val (msg, epc) = runCatching {
                    d.startInventory()
                    Thread.sleep(300)
                    val tags = d.readBufferedTagEvents()
                    d.stopInventory()
                    val snap = tags.firstOrNull()
                    if (snap == null) "No tag found. Move closer and try again." to null
                    else "Tag detected (RSSI=${snap.rssi ?: "?"})." to snap.epc
                }.getOrElse {
                    "Scan failed: ${it.message ?: it.javaClass.simpleName}" to null
                }
                view.post {
                    statusText.text = msg
                    if (!epc.isNullOrBlank()) {
                        if (UhfConfigPrefs.load(requireContext()).soundEnabled) {
                            SoundBeeper.playBeep()
                        }
                        epcInput.setText(epc)
                    }
                }
            }
        }

        writeBtn.setOnClickListener {
            statusText.text = "Writing to memory bank..."
            val bank = parseBank(bankInput.text?.toString())
            val wordPtr = ptrInput.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val dataHex = dataInput.text?.toString()?.trim().orEmpty()
            val epc = epcInput.text?.toString()?.trim()
            val pwd = pwdInput.text?.toString()?.trim()

            bg.execute {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    view.post { statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Check hardware connection." }
                    return@execute
                }

                val (ok, msg) = runCatching {
                    val res = d.writeMemoryBank(
                        epc = epc,
                        bank = bank,
                        ptr = wordPtr,
                        dataHex = dataHex,
                        pwdHex = pwd,
                    )
                    if (res) true to "Write OK." else false to "Write failed (unsupported or rejected)."
                }.getOrElse {
                    false to "Write failed: ${it.message ?: it.javaClass.simpleName}"
                }

                view.post {
                    statusText.text = msg
                    if (ok && UhfConfigPrefs.load(requireContext()).soundEnabled) {
                        SoundBeeper.playBeep()
                    }
                }
            }
        }

        setPwdBtn.setOnClickListener {
            statusText.text = "Setting access password..."
            val epc = epcInput.text?.toString()?.trim()
            val newPwd = newPwdInput.text?.toString()?.trim().orEmpty()
            bg.execute {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    view.post { statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Check hardware connection." }
                    return@execute
                }

                val (ok, msg) = runCatching {
                    val res = d.writeMemoryBank(
                        epc = epc,
                        bank = MemoryBank.RESERVED,
                        ptr = 2,
                        dataHex = newPwd,
                        pwdHex = null
                    )
                    if (res) true to "Access password updated." else false to "Failed to set access password."
                }.getOrElse {
                    false to "Failed to set password: ${it.message ?: it.javaClass.simpleName}"
                }
                view.post {
                    statusText.text = msg
                    if (ok && UhfConfigPrefs.load(requireContext()).soundEnabled) {
                        SoundBeeper.playBeep()
                    }
                }
            }
        }

        lockBtn.setOnClickListener {
            statusText.text = "Locking tag..."
            val epc = epcInput.text?.toString()?.trim()
            val pwd = pwdInput.text?.toString()?.trim()
            bg.execute {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    view.post { statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Check hardware connection." }
                    return@execute
                }

                val (ok, msg) = runCatching {
                    val res = d.setLockState(epc = epc, accessPasswordHex = pwd, lock = true)
                    if (res) true to "Lock command sent." else false to "Lock failed (unsupported or rejected)."
                }.getOrElse {
                    false to "Lock failed: ${it.message ?: it.javaClass.simpleName}"
                }
                view.post {
                    statusText.text = msg
                    if (ok && UhfConfigPrefs.load(requireContext()).soundEnabled) {
                        SoundBeeper.playBeep()
                    }
                }
            }
        }

        unlockBtn.setOnClickListener {
            statusText.text = "Unlocking tag..."
            val epc = epcInput.text?.toString()?.trim()
            val pwd = pwdInput.text?.toString()?.trim()
            bg.execute {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    view.post { statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Check hardware connection." }
                    return@execute
                }

                val (ok, msg) = runCatching {
                    val res = d.setLockState(epc = epc, accessPasswordHex = pwd, lock = false)
                    if (res) true to "Unlock command sent." else false to "Unlock failed (unsupported or rejected)."
                }.getOrElse {
                    false to "Unlock failed: ${it.message ?: it.javaClass.simpleName}"
                }
                view.post {
                    statusText.text = msg
                    if (ok && UhfConfigPrefs.load(requireContext()).soundEnabled) {
                        SoundBeeper.playBeep()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        try {
            driver?.release()
        } catch (_: Exception) {
        }
        driver = null
        isDriverReady = false
        bg.shutdownNow()
        super.onDestroyView()
    }

    private fun ensureBg() {
        if (bg.isShutdown || bg.isTerminated) {
            bg = Executors.newSingleThreadExecutor()
        }
    }

    private fun parseBank(raw: String?): MemoryBank {
        return when (raw?.trim()?.uppercase()) {
            "RESERVED" -> MemoryBank.RESERVED
            "EPC" -> MemoryBank.EPC
            "TID" -> MemoryBank.TID
            "USER" -> MemoryBank.USER
            else -> MemoryBank.USER
        }
    }
}
