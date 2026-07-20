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
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.rfidsoftwares.R
import com.rfidsoftwares.common.prefs.UhfConfigPrefs
import com.rfidsoftwares.rfid.driver.RfidDriverFactory
import com.rfidsoftwares.rfid.driver.RfidReaderDriver
import com.rfidsoftwares.ui.base.BaseScreenFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ReadTagFragment : BaseScreenFragment() {

    override fun screenTitle(): String = "Read RFID Tags"
    override fun screenSubtitle(): String? = "Scan tags in single shot or continuous live mode"
    override fun allowOfflinePanel(): Boolean = false

    private var bg = Executors.newSingleThreadExecutor()
    private var driver: RfidReaderDriver? = null
    private var isDriverReady = false
    private val isContinuousActive = AtomicBoolean(false)

    // Tabular Scanned Tags State
    private val tagList = mutableListOf<ScannedTagRow>()
    private val epcIndexMap = mutableMapOf<String, Int>()
    private var totalReadsCount = 0L
    private lateinit var adapter: ScannedTagAdapter
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    data class ScannedTagRow(
        val index: Int,
        val epc: String,
        var rssi: String,
        var count: Int,
        var timestamp: String
    )

    override fun createBody(inflater: LayoutInflater, container: FrameLayout): View {
        return inflater.inflate(R.layout.body_read_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureBg()

        val radioGroup: RadioGroup = view.findViewById(R.id.readModeRadioGroup)
        val scanBtn: MaterialButton = view.findViewById(R.id.readTagScanButton)
        val clearBtn: MaterialButton = view.findViewById(R.id.readTagClearButton)
        val statusText: TextView = view.findViewById(R.id.readTagStatusText)
        val summaryText: TextView = view.findViewById(R.id.readTagSummaryText)
        val recyclerView: RecyclerView = view.findViewById(R.id.scannedTagsRecyclerView)

        adapter = ScannedTagAdapter(tagList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateSummaryUI(summaryText)

        // Eager Hardware Reader Initialization on Fragment Entry
        statusText.text = "Initializing reader hardware..."
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
                    statusText.text = "Reader ready (${d.displayName}). Select mode and tap scan."
                } else {
                    statusText.text = "⚠️ Hardware Init Failed (${d.displayName}): Could not connect to RFID module. Ensure reader power is ON."
                }
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.readModeContinuousRadio) {
                scanBtn.text = if (isContinuousActive.get()) "Stop Continuous Scan" else "Start Continuous Scan"
            } else {
                if (isContinuousActive.get()) {
                    stopContinuousScan(scanBtn, statusText)
                }
                scanBtn.text = "Scan nearest tag (Single)"
            }
        }

        clearBtn.setOnClickListener {
            if (isContinuousActive.get()) {
                stopContinuousScan(scanBtn, statusText)
            }
            tagList.clear()
            epcIndexMap.clear()
            totalReadsCount = 0L
            adapter.notifyDataSetChanged()
            updateSummaryUI(summaryText)
            statusText.text = "Status: Table cleared."
        }

        scanBtn.setOnClickListener {
            val isContinuousMode = radioGroup.checkedRadioButtonId == R.id.readModeContinuousRadio
            if (isContinuousMode) {
                if (isContinuousActive.get()) {
                    stopContinuousScan(scanBtn, statusText)
                } else {
                    startContinuousScan(scanBtn, statusText, summaryText)
                }
            } else {
                // Single Read Mode
                statusText.text = "Status: Scanning (Single shot)..."
                bg.execute {
                    val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                    if (!isDriverReady) {
                        isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                    }
                    if (!isDriverReady) {
                        view.post {
                            statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Could not connect to physical RFID hardware."
                        }
                        return@execute
                    }

                    val (ok, msg, epc, rssi) = runCatching {
                        d.startInventory()
                        Thread.sleep(300)
                        val tags = d.readBufferedTagEvents()
                        d.stopInventory()
                        val snap = tags.firstOrNull()
                        if (snap == null) Quadruple(false, "Status: No tag found nearby.", null, null)
                        else Quadruple(true, "Status: Tag detected (${snap.rssi ?: "?"} dBm).", snap.epc, snap.rssi?.toString() ?: "?")
                    }.getOrElse {
                        Quadruple(false, "⚠️ Hardware Error: ${it.message ?: it.javaClass.simpleName}", null, null)
                    }

                    view.post {
                        statusText.text = msg
                        if (ok && !epc.isNullOrBlank()) {
                            if (UhfConfigPrefs.load(requireContext()).soundEnabled) {
                                com.rfidsoftwares.common.sound.SoundBeeper.playBeep()
                            }
                            addOrUpdateTagRow(epc, rssi ?: "?")
                            updateSummaryUI(summaryText)
                            recyclerView.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }

    private fun startContinuousScan(
        scanBtn: MaterialButton,
        statusText: TextView,
        summaryText: TextView
    ) {
        if (!isContinuousActive.compareAndSet(false, true)) return
        scanBtn.text = "Stop Continuous Scan"
        statusText.text = "Status: Continuous scan active..."

        bg.execute {
            try {
                val d = driver ?: RfidDriverFactory.createDriver(UhfConfigPrefs.load(requireContext()).driverId).also { driver = it }
                if (!isDriverReady) {
                    isDriverReady = try { d.init(requireContext().applicationContext) } catch (e: Exception) { false }
                }
                if (!isDriverReady) {
                    isContinuousActive.set(false)
                    scanBtn.post {
                        scanBtn.text = "Start Continuous Scan"
                        statusText.text = "⚠️ Reader Init Failed (${d.displayName}): Could not connect to physical RFID hardware."
                    }
                    return@execute
                }

                d.startInventory()
                while (isContinuousActive.get()) {
                    val events = d.readBufferedTagEvents()
                    if (events.isNotEmpty()) {
                        val isSoundEnabled = UhfConfigPrefs.load(requireContext().applicationContext).soundEnabled
                        if (isSoundEnabled) {
                            com.rfidsoftwares.common.sound.SoundBeeper.playBeep()
                        }
                        scanBtn.post {
                            for (event in events) {
                                addOrUpdateTagRow(event.epc, event.rssi?.toString() ?: "?")
                            }
                            updateSummaryUI(summaryText)
                        }
                    }
                    Thread.sleep(100)
                }
                d.stopInventory()
            } catch (e: Exception) {
                isContinuousActive.set(false)
                scanBtn.post {
                    scanBtn.text = "Start Continuous Scan"
                    statusText.text = "Status: Continuous scan error (${e.message})"
                }
            }
        }
    }

    private fun stopContinuousScan(scanBtn: MaterialButton, statusText: TextView) {
        isContinuousActive.set(false)
        scanBtn.text = "Start Continuous Scan"
        statusText.text = "Status: Continuous scan stopped. ${tagList.size} unique tags captured."
    }

    private fun addOrUpdateTagRow(epc: String, rssi: String) {
        totalReadsCount++
        val nowStr = timeFormat.format(Date())
        val existingIndex = epcIndexMap[epc]
        if (existingIndex != null && existingIndex < tagList.size) {
            val existing = tagList[existingIndex]
            existing.rssi = "$rssi dBm"
            existing.count += 1
            existing.timestamp = nowStr
            adapter.notifyItemChanged(existingIndex)
        } else {
            val newRow = ScannedTagRow(
                index = tagList.size + 1,
                epc = epc,
                rssi = "$rssi dBm",
                count = 1,
                timestamp = nowStr
            )
            tagList.add(0, newRow)
            // Re-index map
            epcIndexMap.clear()
            tagList.forEachIndexed { idx, row ->
                epcIndexMap[row.epc] = idx
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateSummaryUI(summaryText: TextView) {
        summaryText.text = "Total: $totalReadsCount | Unique: ${tagList.size}"
    }

    override fun onDestroyView() {
        isContinuousActive.set(false)
        try {
            driver?.stopInventory()
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

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // Inner Adapter for Tabular Tag Rows
    private class ScannedTagAdapter(private val items: List<ScannedTagRow>) :
        RecyclerView.Adapter<ScannedTagAdapter.TagViewHolder>() {

        class TagViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val index: TextView = v.findViewById(R.id.tvRowIndex)
            val epc: TextView = v.findViewById(R.id.tvRowEpc)
            val rssi: TextView = v.findViewById(R.id.tvRowRssi)
            val count: TextView = v.findViewById(R.id.tvRowCount)
            val time: TextView = v.findViewById(R.id.tvRowTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_scanned_tag_table, parent, false)
            return TagViewHolder(v)
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            val item = items[position]
            holder.index.text = (items.size - position).toString()
            holder.epc.text = item.epc
            holder.rssi.text = item.rssi
            holder.count.text = item.count.toString()
            holder.time.text = item.timestamp
        }

        override fun getItemCount(): Int = items.size
    }
}
