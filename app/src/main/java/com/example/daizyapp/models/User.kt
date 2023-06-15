package com.example.daizyapp.models

data class User(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val email: String,
    val profilePicture: String,
    val isVerified: Boolean,
    val firstname: String,
    val lastname: String,
    val bio: String,
    val password: String,
    val updatedAt: String
)