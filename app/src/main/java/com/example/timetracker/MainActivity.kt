package com.example.timetracker

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var taskNameInput: EditText
    private lateinit var timerDisplay: Chronometer
    private lateinit var startStopButton: Button
    private lateinit var recordsContainer: LinearLayout
    
    private var isRunning = false
    private var startTime: Long = 0
    private val records = mutableListOf<TimeRecord>()
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    data class TimeRecord(
        val taskName: String,
        val startTime: Long,
        val endTime: Long,
        val duration: Long
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建主布局
        val scrollView = ScrollView(this)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // 任务名称输入
        taskNameInput = EditText(this).apply {
            hint = "任务名称"
            setPadding(16, 16, 16, 16)
        }
        mainLayout.addView(taskNameInput)
        
        // 计时器显示
        timerDisplay = Chronometer(this).apply {
            textSize = 48f
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        mainLayout.addView(timerDisplay)
        
        // 开始/停止按钮
        startStopButton = Button(this).apply {
            text = "开始计时"
            setOnClickListener { toggleTimer() }
        }
        mainLayout.addView(startStopButton)
        
        // 记录标题
        val recordsTitle = TextView(this).apply {
            text = "计时记录"
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
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            timerDisplay.stop()
            
            val taskName = taskNameInput.text.toString()
            if (taskName.isNotBlank() && duration > 1000) {
                val record = TimeRecord(taskName, startTime, endTime, duration)
                records.add(0, record)
                addRecordView(record)
            }
            
            isRunning = false
            startStopButton.text = "开始计时"
            taskNameInput.isEnabled = true
            taskNameInput.setText("")
        } else {
            // 开始计时
            val taskName = taskNameInput.text.toString()
            if (taskName.isNotBlank()) {
                startTime = System.currentTimeMillis()
                timerDisplay.base = SystemClock.elapsedRealtime()
                timerDisplay.start()
                
                isRunning = true
                startStopButton.text = "停止计时"
                taskNameInput.isEnabled = false
            }
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
        
        val nameText = TextView(this).apply {
            text = record.taskName
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }
        recordView.addView(nameText)
        
        val durationText = TextView(this).apply {
            text = "时长: ${formatDuration(record.duration)}"
            textSize = 14f
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
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
