package com.horizam.pro.elean.data.model.requests

data class AcceptOrderRequest(
    val offer_id: String,
    var token: String
)