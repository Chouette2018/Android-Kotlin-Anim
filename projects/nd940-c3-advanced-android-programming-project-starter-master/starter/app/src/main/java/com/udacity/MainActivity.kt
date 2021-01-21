package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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

    private var downloadID: Long = -1

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var downloadReceiver:DownloadReceiver? = null
    private var url:String= ""
    private var fileName:String= ""
    private lateinit var downloadObserverHandler: Handler
    private var downloadInProgressValue = 0f //value between 0 and 1

    private val onDownloadObserved = object : Runnable {
        override fun run() {
            refreshDownloadInfo()
            downloadObserverHandler.postDelayed(this, 100)
        }
    }

    private fun refreshDownloadInfo(){
        if(isDonwloadInProgress()){
            //compute fraction not %
            downloadInProgressValue = getProgress(downloadID)

            custom_button.setProgress(downloadInProgressValue)

            if(downloadInProgressValue >= 1f){
                onDownloadCompleted()
            }
        }
    }

    fun getProgress(donwloadId:Long):Float{
        val cursor = getDownloadManagerCursor(donwloadId)
        if (cursor.moveToFirst()) {
            val currentSizeIndex: Int =
                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalSizeIndex: Int = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val currentSize = cursor.getInt(currentSizeIndex)
            val totalSize = cursor.getInt(totalSizeIndex)
            Log.e("GDS", "totalSize $totalSize")
            cursor.close()
            if (currentSize == -1) {
                return 0f
            } else {
                return (currentSize / totalSize).toFloat()
            }
        }

        return 0f
    }

    private fun getDownloadManagerCursor(donwloadId:Long): Cursor {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(donwloadId)
        val cursor: Cursor = downloadManager.query(query)
        return cursor
    }

    private fun isDonwloadInProgress():Boolean{
        return downloadID != -1L
    }

    private fun onDownloadCompleted(){
        downloadID = -1L
        downloadInProgressValue = 0f
        downloadObserverHandler.removeCallbacks(onDownloadObserved)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        downloadObserverHandler = Handler(Looper.getMainLooper())
        custom_button.setOnClickListener {
            download()
        }
    }

    override fun onPause() {
        super.onPause()
        if(isDonwloadInProgress()) {
            downloadObserverHandler.removeCallbacks(onDownloadObserved)
        }
    }

    override fun onResume() {
        super.onResume()
        if(isDonwloadInProgress()) {
            downloadObserverHandler.post(onDownloadObserved)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GDS downloader"
            val descriptionText = "To monitor downloads"
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

            downloadObserverHandler.post(onDownloadObserved)
        }
    }

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
