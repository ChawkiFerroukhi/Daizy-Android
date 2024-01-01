package com.example.daizyapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.daizyapp.R
import com.example.daizyapp.models.LoginResponse
import com.example.daizyapp.utils.Utility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HomeActivity : AppCompatActivity() {

    private var loadingDialog =  LoadingDialog(this);

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        replaceFragment(HomeFragment())

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener{ id ->
            when(id.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.addPost -> replaceFragment(NewPostFragment())
                R.id.logout-> {
                    loadingDialog.startLoadingDialog()
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val result = logoutUser()
                            withContext(Dispatchers.Main) {
                                Log.d("logout result: ", result)
                                // Login successful, start the main activity.
                                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                                loadingDialog.stopLoadingDialog()
                            }
                        } catch (error: Throwable) {
                            withContext(Dispatchers.Main) {
                                // Login failed, show an error message.
                                Log.d("Login error: ",error.toString())
                                loadingDialog.stopLoadingDialog()
                                Toast.makeText(this@HomeActivity, "Logout failed, please try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
                else -> {

                }
            }
            true

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        var fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private suspend fun logoutUser() : String {
        return suspendCoroutine { continuation ->
            val gson = Gson()
            val sharedPref = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
            val user = sharedPref.getString(Utility.userKey, null)
            val token = gson.fromJson(user, LoginResponse::class.java).token

            if (user != null) {
                val queue = Volley.newRequestQueue(applicationContext)
                val url = Utility.apiUrl + "/api/logout"

                val request = object : JsonObjectRequest(Method.GET, url, null,
                    { response ->
                        // Logout successful, remove user information from shared preferences
                        val editor = sharedPref.edit()
                        editor.remove(Utility.userKey)
                        editor.apply()

                        continuation.resume(response.toString())

                    },
                    { error ->
                        // Logout failed, handle the error
                        Log.d("Logout error:", error.toString())
                        // You can still remove user information from shared preferences even if logout fails
                        val editor = sharedPref.edit()
                        editor.remove(Utility.userKey)
                        editor.apply()

                        // Redirect to login activity or perform any other required actions after logout
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = token
                        Log.d("test", "token: $token")
                        return headers
                    }
                }

                request.retryPolicy =
                    DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                queue.add(request)
            }
        }
    }



}