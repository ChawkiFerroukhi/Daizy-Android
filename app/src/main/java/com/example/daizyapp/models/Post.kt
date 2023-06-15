package com.example.daizyapp.models

data class Post(
    val __v: Int,
    val _id: String,
    val content: String,
    val createdAt: String,
    val image: String,
    var likes: List<String>,
    val user: User,
)