package com.example.psbtask

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var lastUpdatedTextView: TextView
    private lateinit var currencyListTextView: TextView

    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateLastUpdatedTime()
            handler.postDelayed(this, 30000) // Повторяем каждые 30 секунд
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastUpdatedTextView = findViewById(R.id.last_updated_text_view)
        currencyListTextView = findViewById(R.id.currency_list_text_view)

        // авто обновление времени
        handler.postDelayed(updateTimeRunnable, 30000)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getCurrencyRates()
                if (response.isSuccessful) {
                    val exchangeRates = response.body()
                    val lastUpdated = exchangeRates?.date ?: ""
                    val currencies = exchangeRates?.valute?.map { entry ->
                        "${entry.value.name}: ${entry.value.value} RUB\n"
                    }?.joinToString(separator = "")

                    GlobalScope.launch(Dispatchers.Main) {
                        lastUpdatedTextView.text = "Last updated: $lastUpdated"
                        currencyListTextView.text = currencies
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        currencyListTextView.text = "Failed to fetch data"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalScope.launch(Dispatchers.Main) {
                    currencyListTextView.text = "Error occurred: ${e.message}"
                }
            }
        }
    }

    private fun updateLastUpdatedTime() {
        val currentTimeMoscow = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formattedTime = formatter.format(currentTimeMoscow)
        lastUpdatedTextView.text = "Last updated: $formattedTime"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена авто обновления времени при уничтожении
        handler.removeCallbacks(updateTimeRunnable)
    }
}
