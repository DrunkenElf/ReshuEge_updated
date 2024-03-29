package com.reshuege.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
//import com.ilnur.DownloadTasks.ForegroundService1
import com.reshuege.R
import com.reshuege.backend.Downloaders
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadForeground : Service() {
    internal var download_pictures: Boolean = true

    var subject_prefix: String = ""
    var name: String = ""

    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManagerCompat


    @Inject
    lateinit var downloaders: Downloaders


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

    }

    companion object {
        //lateinit var downloaders: Downloaders
        fun startService(context: Context, prefix: String, name: String, downloaders1: Downloaders) {
            val startIntent = Intent(context.applicationContext, DownloadForeground::class.java)
            startIntent.putExtra("prefix", prefix)
            startIntent.putExtra("name", name)
            ContextCompat.startForegroundService(context.applicationContext, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DownloadForeground::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        subject_prefix = intent!!.getStringExtra("prefix").toString()
        name = intent.getStringExtra("name").toString()

        val position = intent.getIntExtra("position", -1)

        val settingsPref = PreferenceManager.getDefaultSharedPreferences(this)
        download_pictures = settingsPref.getBoolean("download_pictures", false)

        notificationManager = NotificationManagerCompat.from(this.applicationContext)

        notificationBuilder =
            NotificationCompat.Builder(this.applicationContext, (77).toString()).apply {
                setContentTitle("$name: загрузка")
                setSmallIcon(R.mipmap.ic_launcher)
                setStyle(NotificationCompat.BigTextStyle())
                setAutoCancel(false)
            }
        startForeground(1, notificationBuilder.build())

        downloaders.getSubjectService(intent,subject_prefix, name, notificationManager, position)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent)

        super.onTaskRemoved(rootIntent)
    }


}