package com.udacity

const val DOWNLOAD_STARTED = 0
const val DOWNLOAD_IN_PROGRESS = 1
const val DOWNLOAD_COMPLETE = 2

sealed class ButtonState(
    val downloadInProgress: Int,
    val text: String,
    val contentDescription: String
) {
    object Clicked : ButtonState(DOWNLOAD_STARTED, "Download", "Starting download")
    object Loading : ButtonState(DOWNLOAD_IN_PROGRESS, "We are loading", "Download in progress")
    object Completed : ButtonState(DOWNLOAD_COMPLETE, "Download", "Download")
}