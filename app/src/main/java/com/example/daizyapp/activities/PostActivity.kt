package com.example.daizyapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.daizyapp.R
import com.example.daizyapp.models.Post
import com.google.gson.Gson

class PostActivity : AppCompatActivity() {
    private lateinit var likeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        val gson = Gson()
        val course = getIntent().getStringExtra("post")

        val postFromJson =  gson.fromJson(course, Post::class.java)

        val image = findViewById<ImageView>(R.id.imageView4)
        val title = findViewById<TextView>(R.id.courseTitle)
        val contents = findViewById<TextView>(R.id.courseContents)

        Glide.with(this)
            .load(postFromJson.image)
            //.placeholder(R.drawable.illustration_started)
            .into(image)

        //title.text = courseFromJson.title

        contents.text = postFromJson.content
    }
}