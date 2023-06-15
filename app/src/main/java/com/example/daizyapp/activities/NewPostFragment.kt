package com.example.daizyapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import java.io.IOException


class NewPostFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_new_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val submitImageBtn = view.findViewById<Button>(R.id.button2)
        val publishBtn = view.findViewById<Button>(R.id.button5)

        submitImageBtn.setOnClickListener {
            uploadImage()
        }

        publishBtn.setOnClickListener {
            createPost()
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

    private fun createPost() {
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
            Utility.apiUrl + "/api/post/create",
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
                val postContent = view?.findViewById<EditText>(R.id.post_content)
                params["content"] = postContent?.text.toString()
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
}