package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.receiver.DownloadReceiver
import com.udacity.util.DownloadManagerUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var url:String= ""
    private var fileName:String= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        custom_button.setOnClickListener {
            download()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GDS"
            val descriptionText = "To monitor download"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.download_notification_channel_id), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download() {
        if(url.isBlank()){
            Toast.makeText(this, getString(R.string.download_error_hint), Toast.LENGTH_LONG).show()
            custom_button.setProgress(1f)
        }else {
            val request =
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(fileName)
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.

            if(downloadReceiver == null ) {
                downloadReceiver =
                    DownloadReceiver(downloadID, DownloadManagerUtil(downloadManager))
                registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }else{
                downloadReceiver!!.downloadId = downloadID
            }
        }
    }

    private var downloadReceiver:DownloadReceiver? = null

    override fun onDestroy() {
        super.onDestroy()
        downloadReceiver?.let {
            unregisterReceiver(downloadReceiver)
            downloadReceiver = null
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radioGlide ->
                    if (checked) {
                        url = "https://github.com/bumptech/glide"
                        fileName = getString(R.string.glide_label)
                    }
                R.id.radioLoadApp ->
                    if (checked) {
                        url = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
                        fileName = getString(R.string.load_app_label)
                    }
                R.id.radioRetrofit ->
                    if (checked) {
                        url = "https://github.com/square/retrofit"
                        fileName = getString(R.string.retrofit_label)
                    }
            }
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
