package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val fileName = intent.getStringExtra(getString(R.string.file_name_key))
        val status = intent.getStringExtra(getString(R.string.status_key))
        tv_file_field.text = fileName ?: "Unknown"
        tv_status_result.text = status ?: "Unknown"
    }
}
