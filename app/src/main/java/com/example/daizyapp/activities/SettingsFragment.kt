package com.example.daizyapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.AuthFailureError
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.daizyapp.R
import com.example.daizyapp.models.LoginResponse
import com.example.daizyapp.utils.Utility
import com.google.gson.Gson
import com.example.daizyapp.utils.FileDataPart
import com.example.daizyapp.utils.VolleyFileUploadRequest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class SettingsFragment : Fragment() {

    private val client = OkHttpClient()
    private var imageData: ByteArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val submitImageBtn = view.findViewById<Button>(R.id.button2)
        val publishBtn = view.findViewById<Button>(R.id.button5)
        val generatedBioText = view.findViewById<TextView>(R.id.genBioTextView)
        val bioText = view.findViewById<TextView>(R.id.editBioTextView)
        val autoGenerateBtn = view.findViewById<Button>(R.id.autoGenerate)

        autoGenerateBtn.setOnClickListener {
            val command = "Write a 3 blocks long biography about ${bioText.text}"
            getResponse(command) { response ->
                ThreadUtil.runOnUiThread {
                    generatedBioText.setText(response)
                }
            }
        }

        submitImageBtn.setOnClickListener {
            uploadImage()
        }

        publishBtn.setOnClickListener {
            updateProfile()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1){
            val imagedata = data?.data
            if(imagedata != null) {
                val profileImage = view?.findViewById<ImageView>(R.id.imageView2)
                profileImage?.setImageURI(imagedata)
                createImageData(imagedata)
            }
        }
    }

    private fun updateProfile() {
        val gson = Gson()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val user = sharedPref.getString(Utility.userKey, "")
        val token = gson.fromJson(user, LoginResponse::class.java).token

        if (imageData == null) {
            Toast.makeText(context, "Image required", Toast.LENGTH_SHORT).show()
            return
        }

        val request = object : VolleyFileUploadRequest(
            Method.POST,
            Utility.apiUrl + "/api/updateprofile",
            { response ->
                val responseData = String(response.data)
                Log.d("post", "response is: $responseData")

                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, HomeFragment())
                    .commit()
            },
            { error ->
                Log.d("post", "error is: ${error.message}")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token
                Log.d("test", "token: $token")
                return headers
            }

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val bio = view?.findViewById<EditText>(R.id.genBioTextView)
                val firstname = view?.findViewById<EditText>(R.id.firstnameTextView)
                val lastname = view?.findViewById<EditText>(R.id.lastnameTextView)
                params["bio"] = bio?.text.toString()
                params["firstname"] = firstname?.text.toString()
                params["lastname"] = lastname?.text.toString()
                Log.d("test", "params: $params")
                return params
            }

            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()

                // Generate a unique filename
                val timestamp = System.currentTimeMillis().toString()
                val randomNumber = (0..9999).random().toString()
                val filename = "image_$timestamp$randomNumber.jpg"

                params["Image"] = FileDataPart(filename, imageData!!, "image/jpeg")
                Log.d("test", "image params: $params")
                return params
            }
        }

        request.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        Volley.newRequestQueue(context).add(request)

    }


    private fun uploadImage(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri){
        val inputStream = context?.contentResolver?.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }
    private fun getResponse(question: String, callback: (String) -> Unit){
        val apiKey = "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"
        val url = "https://api.openai.com/v1/completions"

        val requestBody = """
            {
            "model": "text-davinci-003",
            "prompt": "$question",
            "max_tokens": 30,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "Api failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if(body != null) {
                    Log.v("data", body)
                }else{
                    Log.v("data", "empty")
                }
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }

        })
    }
}

object ThreadUtil {
    private val handler = Handler(Looper.getMainLooper())

    fun runOnUiThread(action: () -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(action)
        } else {
            action.invoke()
        }
    }
}