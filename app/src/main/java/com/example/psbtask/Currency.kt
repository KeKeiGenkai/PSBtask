package com.example.psbtask

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("daily_json.js")
    suspend fun getCurrencyRates(): Response<ExchangeRatesResponse>
}

data class ExchangeRatesResponse(
    @SerializedName("Date")
    val date: String, // дата последнего обновления
    @SerializedName("Valute")
    val valute: Map<String, Currency> // информация о валютах
)

data class Currency(
    @SerializedName("CharCode")
    val charCode: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Value")
    val value: Double
)