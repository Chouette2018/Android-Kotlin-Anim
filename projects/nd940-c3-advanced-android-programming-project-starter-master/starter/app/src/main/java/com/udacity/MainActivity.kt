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
import kotlinx.coroutines.*
import java.lang.Runnable


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
    private var fileSize = 0L
    val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val onDownloadObserved = object : Runnable {
        override fun run() {
            refreshDownloadInfo()
            downloadObserverHandler.postDelayed(this, 50)
        }
    }

    private fun refreshDownloadInfo(){
        if(isDonwloadInProgress()){
            //compute fraction not %
            scope.launch {
                downloadInProgressValue = getProgress(downloadID)

                custom_button.setProgress(downloadInProgressValue)

                if (downloadInProgressValue >= 1f) {
                    onDownloadCompleted()
                }
            }
        }
    }

    private var currentPortion = 0
    suspend fun getProgress(donwloadId:Long):Float{
        var currentSize = 0
        var totalSize = -1

        withContext(Dispatchers.IO) {
            val cursor = getDownloadManagerCursor(donwloadId)
            if (cursor.moveToFirst()) {
                val currentSizeIndex: Int =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalSizeIndex: Int =
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                currentSize = cursor.getInt(currentSizeIndex)
                totalSize = cursor.getInt(totalSizeIndex)
                Log.e("GDS", "totalSize $totalSize")
                cursor.close()
            }
        }

        if (currentSize == totalSize) {
            return 1f
        }

        currentPortion += 9

        currentPortion = if(currentPortion >= 100) 0 else currentPortion

        return currentPortion/100f
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
        fileSize = 0L
        currentPortion = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        createNotificationChannel()
        downloadObserverHandler = Handler(Looper.getMainLooper())
        custom_button.setOnClickListener {
            scope.launch{
                download()
            }
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

    private suspend fun download() {
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

            fileSize = DownloadManagerUtil.getFileSizeOfUrl(url)

            downloadObserverHandler.post(onDownloadObserved)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadReceiver?.let {
            unregisterReceiver(downloadReceiver)
            downloadReceiver = null
        }
        scope.cancel()
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
    }
}
