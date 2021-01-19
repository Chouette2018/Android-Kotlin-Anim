package com.udacity.receiver

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.udacity.R
import com.udacity.util.DownloadManagerUtil
import com.udacity.util.sendNotification

class DownloadReceiver(var downloadId: Long, val dmu: DownloadManagerUtil?) : BroadcastReceiver() {

    //default constructor required for being a receiver.
    constructor() : this(-1, null)

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val currentDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if(currentDownloadId >=0
                && currentDownloadId == downloadId) {
                    sendNotification(context)
                }
            }
            else -> return
        }
    }

    private fun sendNotification(context: Context){
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(
            context.getText(R.string.download_complete).toString(),
            dmu!!.getFileName(downloadId),
            dmu!!.getFileStatus(downloadId),
            context
        )
    }
}