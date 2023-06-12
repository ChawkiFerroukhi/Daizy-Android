package com.example.daizyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.daizyapp.activities.LoginActivity
import com.example.daizyapp.utils.Utility
import java.util.Timer
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)


        Timer().schedule(3000){
            val isLoggedIn = sharedPref.getString(Utility.userKey, "")

            if(!isLoggedIn.isNullOrEmpty()){
                //startActivity(Intent(applicationContext, ProfileActivity::class.java))
            } else {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
            finish()
        }
    }
}