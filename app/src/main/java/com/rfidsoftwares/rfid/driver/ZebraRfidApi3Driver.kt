/*
 * Copyright (c) 2026 RFID Software India Private Limited (https://rfidsoftwares.com)
 * OpenRFID Handheld Android - Universal Multi-Vendor Handheld RFID Middleware & Suite
 */

package com.rfidsoftwares.rfid.driver

import android.content.Context
import com.rfidsoftwares.rfid.ReaderDiagnosticsSummary
import com.rfidsoftwares.rfid.TagEvent
import com.rfidsoftwares.rfid.TagSource
import java.util.Locale

/**
 * Driver implementation for Zebra RFID readers (MC3300R, TC20/25, RFD8500, RFD40/90).
 *
 * Uses `com.zebra.rfid.api3.Readers` and `RFIDReader` APIs.
 */
class ZebraRfidApi3Driver : RfidReaderDriver {

    override val driverId: String = "zebra_rfidapi3"
    override val displayName: String = "Zebra RFID (MC3300R / RFD8500 / RFD40)"

    private var reader: Any? = null
    private var isScanning = false

    override fun init(context: Context): Boolean {
        return try {
            val readersClass = Class.forName("com.zebra.rfid.api3.Readers")
            val readersObj = readersClass.getConstructor(Context::class.java).newInstance(context)
            val getAvailableList = readersClass.getMethod("GetAvailableRFIDReaderList")
            val list = getAvailableList.invoke(readersObj) as? List<*>

            if (!list.isNullOrEmpty()) {
                val firstDevice = list[0]
                val getRfidReader = firstDevice?.javaClass?.getMethod("getRFIDReader")
                val r = getRfidReader?.invoke(firstDevice)
                if (r != null) {
                    val connectMethod = r.javaClass.getMethod("connect")
                    connectMethod.invoke(r)
                    reader = r
                    return true
                }
            }
            false
        } catch (_: Exception) {
            reader = null
            false
        }
    }

    override fun release(): Boolean {
        val r = reader ?: return false
        return try {
            val disconnectMethod = r.javaClass.getMethod("disconnect")
            disconnectMethod.invoke(r)
            reader = null
            isScanning = false
            true
        } catch (_: Exception) {
            reader = null
            false
        }
    }

    override fun startInventory(): Boolean {
        val r = reader ?: return false
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val inventory = actions.javaClass.getField("Inventory").get(actions)
            val performMethod = inventory.javaClass.getMethod("perform")
            performMethod.invoke(inventory)
            isScanning = true
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun stopInventory(): Boolean {
        val r = reader ?: return false
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val inventory = actions.javaClass.getField("Inventory").get(actions)
            val stopMethod = inventory.javaClass.getMethod("stop")
            stopMethod.invoke(inventory)
            isScanning = false
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun readBufferedTagEvents(): List<TagEvent> {
        val r = reader ?: return emptyList()
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val getTags = actions.javaClass.getMethod("getReadTags", Int::class.javaPrimitiveType)
            val tags = getTags.invoke(actions, 1) as? Array<*> ?: return emptyList()

            if (tags.isNotEmpty()) {
                val tag = tags[0] ?: return emptyList()
                val getTagID = tag.javaClass.getMethod("getTagID")
                val epc = (getTagID.invoke(tag) as? String)?.trim()?.uppercase(Locale.US) ?: return emptyList()

                val getPeakRSSI = tag.javaClass.getMethod("getPeakRSSI")
                val rssi = (getPeakRSSI.invoke(tag) as? Number)?.toInt()

                listOf(
                    TagEvent(
                        epc = epc,
                        rssi = rssi,
                        source = TagSource.EPC,
                        seenAt = System.currentTimeMillis()
                    )
                )
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun readMemoryBank(
        epc: String?,
        bank: MemoryBank,
        ptr: Int,
        len: Int,
        pwdHex: String?
    ): String? {
        val r = reader ?: return null
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val tagAccess = actions.javaClass.getField("TagAccess").get(actions)
            val readWaitMethod = tagAccess.javaClass.methods.firstOrNull { it.name == "readWait" }
            val result = readWaitMethod?.invoke(tagAccess, epc, bank.bankId, ptr, len, pwdHex)
            result?.toString()
        } catch (_: Exception) {
            null
        }
    }

    override fun writeMemoryBank(
        epc: String?,
        bank: MemoryBank,
        ptr: Int,
        dataHex: String,
        pwdHex: String?
    ): Boolean {
        val r = reader ?: return false
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val tagAccess = actions.javaClass.getField("TagAccess").get(actions)
            val writeWaitMethod = tagAccess.javaClass.methods.firstOrNull { it.name == "writeWait" }
            (writeWaitMethod?.invoke(tagAccess, epc, bank.bankId, ptr, dataHex, pwdHex) as? Boolean) ?: false
        } catch (_: Exception) {
            false
        }
    }

    override fun setLockState(
        epc: String?,
        accessPasswordHex: String?,
        lock: Boolean
    ): Boolean {
        val r = reader ?: return false
        return try {
            val actions = r.javaClass.getField("Actions").get(r)
            val tagAccess = actions.javaClass.getField("TagAccess").get(actions)
            val lockWaitMethod = tagAccess.javaClass.methods.firstOrNull { it.name == "lockWait" }
            (lockWaitMethod?.invoke(tagAccess, epc, accessPasswordHex, if (lock) 1 else 0) as? Boolean) ?: false
        } catch (_: Exception) {
            false
        }
    }

    override fun setPower(powerDbm: Int): Boolean {
        val r = reader ?: return false
        return try {
            val config = r.javaClass.getField("Config").get(r)
            val setAntennaConfig = config.javaClass.methods.firstOrNull { it.name == "setAntennaPower" || it.name == "setPower" }
            setAntennaConfig?.invoke(config, powerDbm)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun getPower(): Int? {
        val r = reader ?: return null
        return try {
            val config = r.javaClass.getField("Config").get(r)
            val getAntennaConfig = config.javaClass.methods.firstOrNull { it.name == "getAntennaPower" || it.name == "getPower" }
            (getAntennaConfig?.invoke(config) as? Number)?.toInt()
        } catch (_: Exception) {
            null
        }
    }

    override fun readDiagnosticsSummary(): ReaderDiagnosticsSummary {
        val power = getPower()
        return ReaderDiagnosticsSummary(
            sdkReady = reader != null,
            readerOpen = reader != null,
            powerDbm = power,
            regionOrFrequency = "Zebra RFID API3",
            batteryNote = "Zebra Reader Connected",
            detailLine = "Zebra RFID API3 Driver Active"
        )
    }
}
