package com.zigsaadvertisement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class Logout : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        val sessionManager = SessionManager(this)

        val logout = findViewById<Button>(R.id.logoutAccount)
        logout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("zigsa_advertisement", Context.MODE_PRIVATE)
            val editorDelete = sharedPreferences.edit()
            editorDelete.remove("token")
            editorDelete.apply()
            val intent = Intent(this@Logout, Login::class.java)
            startActivity(intent)
            finish()
        }

        val buildNumber = BuildConfig.VERSION_NAME
        val buildCode = BuildConfig.VERSION_CODE
        val buildNumberText = findViewById<TextView>(R.id.build_number)
        val buildCodeText = findViewById<TextView>(R.id.build_code)
        buildNumberText?.text = "Build number is: $buildNumber"
        buildCodeText?.text = "Build code is: $buildCode"
    }
}