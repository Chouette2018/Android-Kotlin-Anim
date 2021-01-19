package com.udacity.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.DetailActivity
import com.udacity.R

private const val NOTIFICATION_ID = 13


fun NotificationManager.sendNotification(messageBody: String,
                                         fileName: String,
                                         status: String,
                                         applicationContext: Context) {
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.putExtra(applicationContext.getString(R.string.file_name_key), fileName)
    contentIntent.putExtra(applicationContext.getString(R.string.status_key), status)

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext
            .getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(R.drawable.ic_assistant_black_24dp,"Check download status", contentPendingIntent)

    with(NotificationManagerCompat.from(applicationContext)) {
        notify(NOTIFICATION_ID, builder.build())
    }
}