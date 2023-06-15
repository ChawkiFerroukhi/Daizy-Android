package com.example.daizyapp.viewholder

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.daizyapp.R
import com.example.daizyapp.models.LoginResponse
import com.example.daizyapp.models.Post
import com.example.daizyapp.utils.Utility
import com.google.gson.Gson
import org.w3c.dom.Text

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val avatarImageView : ImageView = itemView.findViewById(R.id.avatarImageView);
    val postImageView : ImageView = itemView.findViewById(R.id.postImageView);
    val contentTextView : TextView = itemView.findViewById(R.id.contentTextView);
    val usernameTextView : TextView = itemView.findViewById(R.id.usernameTextView);
    val createdAtTextView : TextView = itemView.findViewById(R.id.createdAtTextView);
    var postLikestextView : TextView = itemView.findViewById(R.id.postLikes);
    val likeButton : Button = itemView.findViewById(R.id.likeButton);


    fun bind(post: Post) {
        usernameTextView.text = post.user.firstname
        Glide.with(itemView.context).load(post.image).into(postImageView)
        //titleTextView.text = post.title
        contentTextView.text = post.content
        postLikestextView.text = post.likes.size.toString()
        createdAtTextView.text = post.createdAt
        Glide.with(itemView.context).load(post.user.profilePicture).into(avatarImageView)



    }


}