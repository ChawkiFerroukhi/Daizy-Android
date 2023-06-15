package com.example.daizyapp.models

data class LoginResponse(
    val token: String,
    val status: String,
    val user: User
)