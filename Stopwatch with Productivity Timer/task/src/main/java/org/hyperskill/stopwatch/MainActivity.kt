package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible

const val CHANNEL_ID = "org.hyperskill"
const val NOTIFICATION_ID = 393939

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var timerTextView: TextView
        lateinit var startButton: Button
        lateinit var resetButton: Button
        lateinit var settingsButton: Button
        lateinit var progressBar: ProgressBar
        var upperLimit: Int? = null

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        settingsButton = findViewById(R.id.settingsButton)
        progressBar = findViewById(R.id.progressBar)

        createNotificationChannel()

        val handler = Handler(Looper.getMainLooper())
        var counterForSeconds = 0
        var counterForMinutes = 0

        var isTimerRunning = false
        var colorIndex = 0

        timerTextView.text = "00:00"

        val colorListForProgressBar = listOf(
            0XFF243642.toInt(),
            0xFF387478.toInt(),
            0xFF629584.toInt(),
            0XFF4A4947.toInt(),
            0XFFE7CCCC.toInt()
        )

        val timerRunnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run() {
                counterForSeconds++

                if (counterForSeconds == 60) {
                    counterForMinutes++
                    counterForSeconds = 0
                }

                if (counterForMinutes < 25) {
                    timerTextView.text =
                        String.format("%02d:%02d", counterForMinutes, counterForSeconds)

                    if (upperLimit != null) {
                        if (counterForSeconds > upperLimit!!) timerTextView.setTextColor(Color.RED)
                        if (counterForSeconds == upperLimit) createAndShowNotification()
                    }

                    val color = colorListForProgressBar[colorIndex % colorListForProgressBar.size]
                    progressBar.indeterminateTintList = ColorStateList.valueOf(color)
                    colorIndex++

                    handler.postDelayed(this, 1000)

                } else {
                    isTimerRunning = false
                }
            }

        }

        startButton.setOnClickListener {
            if (!isTimerRunning) {
                counterForMinutes = 0
                counterForSeconds = 0

                isTimerRunning = true
                settingsButton.isEnabled = false

                handler.postDelayed(timerRunnable, 1000)
                progressBar.isVisible = true
            }
        }

        resetButton.setOnClickListener {
            timerTextView.text = "00:00"
            isTimerRunning = false
            handler.removeCallbacks(timerRunnable)
            progressBar.isVisible = false
            settingsButton.isEnabled = true
            timerTextView.setTextColor(Color.BLACK)
        }

        settingsButton.setOnClickListener {
            val contentView =
                LayoutInflater.from(this).inflate(R.layout.dialog_content_view, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK") { _, _ ->
                    val upperLimitEditText =
                        contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    upperLimit = upperLimitEditText.text.toString().toInt()
                }
                .show()
        }
        /*
            Tests for android can not guarantee the correctness of solutions that make use of
            mutation on "static" variables to keep state. You should avoid using those.
            Consider "static" as being anything on kotlin that is transpiled to java
            into a static variable. That includes global variables and variables inside
            singletons declared with keyword object, including companion object.
            This limitation is related to the use of JUnit on tests. JUnit re-instantiate all
            instance variable for each test method, but it does not re-instantiate static variables.
            The use of static variable to hold state can lead to state from one test to spill over
            to another test and cause unexpected results.
            Using mutation on static variables to keep state
            is considered a bad practice anyway and no measure
            attempting to give support to that pattern will be made.
         */


    }
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stopwatch Notification"
            val descriptionText = "Your time status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createAndShowNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Stopwatch")
            .setContentText("Time exceeded")
            .setOnlyAlertOnce(true)
            .build()

        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}


