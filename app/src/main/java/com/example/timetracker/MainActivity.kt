package com.example.timetracker

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var timerDisplay: TextView
    private lateinit var startStopButton: Button
    private lateinit var statsButton: Button
    private lateinit var clearButton: Button
    private lateinit var recordsContainer: LinearLayout
    private lateinit var recordsTitle: TextView
    
    private var isRunning = false
    private var startTime: Long = 0
    private val records = mutableListOf<TimeRecord>()
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())
    
    private val updateTimer = object : Runnable {
        override fun run() {
            if (isRunning) {
                val current = System.currentTimeMillis() - startTime
                timerDisplay.text = formatDurationWithMs(current)
                handler.postDelayed(this, 10)
            }
        }
    }
    
    data class TimeRecord(
        val startTime: Long,
        val endTime: Long,
        val duration: Long
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scrollView = ScrollView(this)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // 计时器显示
        timerDisplay = TextView(this).apply {
            text = "00:00:00.000"
            textSize = 48f
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        mainLayout.addView(timerDisplay)
        
        // 按钮行
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        startStopButton = Button(this).apply {
            text = "开始计时"
            setOnClickListener { toggleTimer() }
        }
        buttonLayout.addView(startStopButton)
        
        statsButton = Button(this).apply {
            text = "统计"
            setOnClickListener { showStats() }
        }
        buttonLayout.addView(statsButton)
        
        clearButton = Button(this).apply {
            text = "清理"
            setOnClickListener { clearRecords() }
        }
        buttonLayout.addView(clearButton)
        
        mainLayout.addView(buttonLayout)
        
        // 记录标题
        recordsTitle = TextView(this).apply {
            text = "计时记录 (0)"
            textSize = 18f
            setPadding(0, 32, 0, 16)
        }
        mainLayout.addView(recordsTitle)
        
        // 记录容器
        recordsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        mainLayout.addView(recordsContainer)
        
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun toggleTimer() {
        if (isRunning) {
            // 停止计时
            handler.removeCallbacks(updateTimer)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            if (duration > 100) {
                val record = TimeRecord(startTime, endTime, duration)
                records.add(0, record)
                addRecordView(record)
                updateRecordsTitle()
            }
            
            isRunning = false
            startStopButton.text = "开始计时"
            timerDisplay.text = formatDurationWithMs(duration)
        } else {
            // 开始计时
            startTime = System.currentTimeMillis()
            isRunning = true
            startStopButton.text = "停止计时"
            handler.post(updateTimer)
        }
    }
    
    private fun addRecordView(record: TimeRecord) {
        val recordView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        val durationText = TextView(this).apply {
            text = "时长: ${formatDurationWithMs(record.duration)}"
            textSize = 16f
            setTextColor(0xFF6750A4.toInt())
        }
        recordView.addView(durationText)
        
        val timeText = TextView(this).apply {
            text = "${dateFormat.format(Date(record.startTime))} - ${dateFormat.format(Date(record.endTime))}"
            textSize = 12f
            setTextColor(0xFF79747E.toInt())
        }
        recordView.addView(timeText)
        
        recordsContainer.addView(recordView, 0)
    }
    
    private fun updateRecordsTitle() {
        recordsTitle.text = "计时记录 (${records.size})"
    }
    
    private fun formatDurationWithMs(durationMs: Long): String {
        val ms = durationMs % 1000
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms)
    }
    
    private fun showStats() {
        if (records.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("统计")
                .setMessage("暂无记录")
                .setPositiveButton("确定", null)
                .show()
            return
        }
        
        val durations = records.map { it.duration }
        val min = durations.minOrNull() ?: 0
        val max = durations.maxOrNull() ?: 0
        val avg = durations.average().toLong()
        val total = durations.sum()
        
        val message = """
            记录数: ${records.size}
            
            最短: ${formatDurationWithMs(min)}
            最长: ${formatDurationWithMs(max)}
            平均: ${formatDurationWithMs(avg)}
            总计: ${formatDurationWithMs(total)}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("统计信息")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun clearRecords() {
        AlertDialog.Builder(this)
            .setTitle("清理记录")
            .setMessage("确定要清空所有记录吗？")
            .setPositiveButton("确定") { _, _ ->
                records.clear()
                recordsContainer.removeAllViews()
                updateRecordsTitle()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}