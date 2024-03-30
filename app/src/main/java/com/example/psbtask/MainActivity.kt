package com.example.psbtask

import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var lastUpdatedTextView: TextView
    private lateinit var currencyListTextView: TextView
    private lateinit var progressBar: ProgressBar

    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateLastUpdatedTime()
            handler.postDelayed(this, 30000) // повторять каждые 30 секунд
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastUpdatedTextView = findViewById(R.id.last_updated_text_view)
        currencyListTextView = findViewById(R.id.currency_list_text_view)
        progressBar = findViewById(R.id.progress_bar)

        handler.postDelayed(updateTimeRunnable, 30000)

        // Проверяем, есть ли сохранённое состояние
        savedInstanceState?.let {
            val lastUpdatedTime = it.getString("lastUpdatedTime")
            lastUpdatedTextView.text = "Last updated: $lastUpdatedTime"
        }

        loadData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("lastUpdatedTime", lastUpdatedTextView.text.toString())
    }

    private fun loadData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        progressBar.visibility = ProgressBar.VISIBLE // показать бар

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
                        progressBar.visibility = ProgressBar.GONE
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        currencyListTextView.text = "Failed to fetch data"
                        progressBar.visibility = ProgressBar.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalScope.launch(Dispatchers.Main) {
                    currencyListTextView.text = "Error occurred: ${e.message}"
                    progressBar.visibility = ProgressBar.GONE
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
        handler.removeCallbacks(updateTimeRunnable)
    }
}
