package com.example.daizyapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.daizyapp.R
import com.example.daizyapp.utils.Utility
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class LoginActivity : AppCompatActivity() {
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private var loadingDialog =  LoadingDialog(this);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)


        loginButton.setOnClickListener{
            loadingDialog.startLoadingDialog()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val result = loginUser()
                    withContext(Dispatchers.Main) {
                        Log.d("Login result: ", result)
                        // Login successful, start the main activity.
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        loadingDialog.stopLoadingDialog()
                    }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) {
                        // Login failed, show an error message.
                        Log.d("Login error: ",error.toString())
                        loadingDialog.stopLoadingDialog()
                        Toast.makeText(this@LoginActivity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        registerButton.setOnClickListener{
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }
    }

    private suspend fun loginUser(): String {
        return suspendCoroutine { continuation ->
            val queue = Volley.newRequestQueue(applicationContext)
            val url = Utility.apiUrl + "/api/login"
            val requestBody = JSONObject()
            requestBody.put("email", emailField.text.toString())
            requestBody.put("password", passwordField.text.toString())
            val request = JsonObjectRequest(Request.Method.POST, url, requestBody,
                { response ->
                    Log.d("Response ",response.toString())
                    // Login successful, complete the continuation with the response.
                    val sharedPref = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString(Utility.userKey, response.toString())
                    editor.apply()


                    continuation.resume(response.toString())
                },
                { error ->
                    // Login failed, complete the continuation with an exception.
                    Log.d("Login error: ",error.toString())
                    continuation.resumeWithException(error)
                })
            request.retryPolicy = DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(request)
        }
    }


}