package com.udacity.util

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService


data class DownloadManagerUtil(val downloadManager:DownloadManager){

    fun getFileStatus(donwloadId:Long):String{
        val c = getDownloadManagerCursor(donwloadId)
        if (c.moveToFirst()) {
            val columnIndex: Int = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)
            ) {
                //view.setImageURI(Uri.parse(uriString))
                return "Completed"
            }
        }
        c.close()

        return "Unknown"
    }

    fun getFileName(donwloadId:Long):String{
        val c = getDownloadManagerCursor(donwloadId)
        if (c.moveToFirst()) {
            val columnIndex: Int = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)
            ) {
                return c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
            }
        }
        c.close()

        return "Unknown"
    }

    private fun getDownloadManagerCursor(donwloadId:Long): Cursor{
        val query = DownloadManager.Query()
        query.setFilterById(donwloadId)
        val cursor: Cursor = downloadManager.query(query)
        return cursor
    }
}