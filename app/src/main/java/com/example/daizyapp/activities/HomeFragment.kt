package com.example.daizyapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.util.Util
import com.example.daizyapp.R
//import com.example.chemistryapp.adapters.PostAdapter
//import com.example.chemistryapp.models.Course
//import com.example.chemistryapp.models.GetCourseResponse
import com.example.daizyapp.models.LoginResponse
import com.example.daizyapp.utils.Utility
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine



class HomeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = getPosts(context)
                withContext(Dispatchers.Main) {
                    val postRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                    postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    postRecyclerView.adapter = PostAdapter(result)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT)
            }
        }
    }

    private suspend fun getPosts(context: Context) : List<Course> = suspendCoroutine { continuation ->

        val gson = Gson()
        val url = Utility.apiUrl + "/api/course"
        val sharedPref : SharedPreferences = context.getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val user = sharedPref.getString(Utility.userKey, "")
        val token = gson.fromJson(user, LoginResponse::class.java).accessToken

        val request = object : JsonObjectRequest(
            Method.GET, url, null,
        Response.Listener { response ->
                val postResponse = gson.fromJson(response.toString(), GetCourseResponse::class.java)
                continuation.resume(postResponse.courses)
         },
           Response.ErrorListener { error ->
                continuation.resumeWithException(error)
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return  headers
            }
        }



        Volley.newRequestQueue(context).add(request)

    }


}