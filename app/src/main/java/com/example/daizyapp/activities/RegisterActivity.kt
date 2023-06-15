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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var lastnameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var passwordConfirmField: EditText
    private lateinit var registerButton: Button

    private var loadingDialog =  LoadingDialog(this);


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameField = findViewById(R.id.name)
        emailField = findViewById(R.id.email)
        lastnameField = findViewById(R.id.phone)
        passwordField = findViewById(R.id.password)
        passwordConfirmField = findViewById(R.id.passwordConfirm)
        registerButton = findViewById(R.id.register_button)

        registerButton.setOnClickListener {
            if (passwordField.text.toString() == passwordConfirmField.text.toString()) {
                loadingDialog.startLoadingDialog()
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val result = registerUser()
                        withContext(Dispatchers.Main) {
                            Log.d("Register result", result)

                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                            loadingDialog.stopLoadingDialog()
                        }
                    } catch (error: Throwable) {
                        withContext(Dispatchers.Main) {
                            Log.d("Register error", error.toString())
                            loadingDialog.stopLoadingDialog()
                            Toast.makeText(this@RegisterActivity, "Login failed, please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Passwords don't match, please confirm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun registerUser(): String {
        return suspendCoroutine { continuation ->

            val queue = Volley.newRequestQueue(applicationContext)
            val url = Utility.apiUrl + "/api/register"
            val requestBody = JSONObject()

            requestBody.put("firstname", nameField.text.toString())
            requestBody.put("lastname", lastnameField.text.toString())
            requestBody.put("email", emailField.text.toString())
            requestBody.put("password", passwordField.text.toString())
            requestBody.put("confirmPassword", passwordConfirmField.text.toString())

            val request = JsonObjectRequest(Request.Method.POST, url, requestBody,
                { response ->
                    val sharedPref = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString(Utility.userKey, response.toString())
                    editor.apply()
                    continuation.resume(response.toString())
                },
                { error ->
                    Log.d("Register error:", error.toString())
                    continuation.resumeWithException(error)
                })
            request.retryPolicy =
                DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(request)

        }
    }
}