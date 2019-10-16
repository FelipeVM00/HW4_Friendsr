package com.uan.hw4_friendsr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
