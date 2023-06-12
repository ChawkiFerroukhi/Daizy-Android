package com.example.daizyapp.models

data class LoginResponse(
    val accessToken: String,
    val status: String,
    val user: User
)