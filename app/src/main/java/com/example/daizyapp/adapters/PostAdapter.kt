package com.example.daizyapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.daizyapp.R
import com.example.daizyapp.models.LoginResponse
import com.example.daizyapp.models.Post
import com.example.daizyapp.utils.Utility
import com.example.daizyapp.viewholder.PostViewHolder
import com.google.gson.Gson
import org.w3c.dom.Text

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)

        holder.likeButton.setOnClickListener {
            likePost(post, holder.postLikestextView, holder.likeButton)
        }


        if (isCurrentUserPostOwner(post)) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                deletePost(post)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private fun isCurrentUserPostOwner(post: Post): Boolean {
        val gson = Gson()
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val user = sharedPref.getString(Utility.userKey, "")
        val currentUserId = gson.fromJson(user, LoginResponse::class.java).user._id

        return currentUserId == post.user._id
    }

    private fun likePost(post: Post, postLikestextView: TextView, likeButton: Button) {
        val gson = Gson()
        val queue = Volley.newRequestQueue(context)
        val url = Utility.apiUrl + "/api/post/${post._id}/like"
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val user = sharedPref.getString(Utility.userKey, "")
        val currentUserId = gson.fromJson(user, LoginResponse::class.java).user._id
        val token = gson.fromJson(user, LoginResponse::class.java).token

        val hasLiked = post.likes.contains(currentUserId)

        val request = object : JsonObjectRequest(Method.POST, url, null,
            { response ->
                // Like/unlike successful, handle the response if needed

                // Update the likes count and likes list
                val updatedLikesCount = if (hasLiked) {
                    post.likes = post.likes.filterNot { it == currentUserId }
                    post.likes.size
                } else {
                    post.likes = post.likes.plus(currentUserId)
                    post.likes.size
                }
                postLikestextView.text = updatedLikesCount.toString()

                val likeButtonText = if (hasLiked) {
                    "Like"
                } else {
                    "Unlike"
                }
                likeButton.text = likeButtonText

                Log.d("Response", response.toString())
            },
            { error ->
                // Like/unlike failed, handle the error
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token
                return headers
            }
        }

        queue.add(request)
    }

    private fun deletePost(post: Post) {
        val gson = Gson()
        val queue = Volley.newRequestQueue(context)
        val url = "${Utility.apiUrl}/api/post/${post._id}/delete"
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val user = sharedPref.getString(Utility.userKey, "")
        val token = gson.fromJson(user, LoginResponse::class.java).token

        val request = object : JsonObjectRequest(Method.DELETE, url, null,
            { response ->
                // Post deletion successful, handle the response if needed

                // Remove the post from the list and notify the adapter
                val updatedPosts = posts.toMutableList()
                updatedPosts.remove(post)
                posts = updatedPosts.toList()
                notifyDataSetChanged()

                Log.d("Response", response.toString())
            },
            { error ->
                // Post deletion failed, handle the error
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = token
                return headers
            }
        }

        queue.add(request)
    }


}
