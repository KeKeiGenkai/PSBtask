package com.example.psbtask

import retrofit2.http.GET

interface CurrencyApiService {
    @GET("daily_json.js")
    suspend fun getCurrencies(): CurrencyResponse
}
