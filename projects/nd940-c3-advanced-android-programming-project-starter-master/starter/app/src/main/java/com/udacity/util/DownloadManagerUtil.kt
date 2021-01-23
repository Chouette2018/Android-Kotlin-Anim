package com.udacity.util

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.database.Cursor
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection


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

    companion object {
        @WorkerThread
        suspend fun getFileSizeOfUrl(url: String): Long = withContext(Dispatchers.IO) {
            var urlConnection: URLConnection? = null
            try {
                val uri = URL(url)
                urlConnection = uri.openConnection()
                urlConnection!!.connect()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    return@withContext urlConnection.contentLengthLong
                val contentLengthStr = urlConnection.getHeaderField("content-length")
                return@withContext if (contentLengthStr.isNullOrEmpty()) -1L else contentLengthStr.toLong()
            } catch (ignored: Exception) {
            } finally {
                if (urlConnection is HttpURLConnection)
                    urlConnection.disconnect()
            }
            return@withContext -1L
        }
    }
}