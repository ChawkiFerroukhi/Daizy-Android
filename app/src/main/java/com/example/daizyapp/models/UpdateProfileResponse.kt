package com.example.daizyapp.models

data class UpdateProfileResponse(
    val statusCode: Int,
    val message: String,
    val user: User
)