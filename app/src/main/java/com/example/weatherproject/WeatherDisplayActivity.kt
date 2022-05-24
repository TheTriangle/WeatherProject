package com.example.weatherproject

import android.content.Context
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException


class WeatherDisplayActivity : AppCompatActivity() {
    lateinit var city: String
    var lat: Double = 0.0
    var long: Double = 0.0
    val owmApiKey = "ced99c1bbb8e1f79124ca31436495ae2"

    lateinit var ivWeatherMain: ImageView
    lateinit var tvCityName: TextView
    lateinit var ctx: Context
    var forecastslist: ArrayList<Forecast> = ArrayList()
    lateinit var recycler: RecyclerView
    // private val client = OkHttpClient()
    lateinit var listAdapter: ForecastAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        if (CitySelectActivity.useAlternativeTheme) {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Light, true)
        } else {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Dark, true)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_display)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        ctx = this
        // ivWeatherMain = findViewById(R.id.ivWeatherMain)
        tvCityName = findViewById(R.id.tvCityName)
        city = intent.getStringExtra("city").toString()
        tvCityName.setText("${getString(R.string.weatherin)} $city")

        recycler = findViewById(R.id.rvForecast)
        listAdapter = ForecastAdapter(forecastslist, this)
        recycler.setAdapter(listAdapter)
        val layoutManager = LinearLayoutManager(this)
        recycler.setLayoutManager(layoutManager)


        Log.d("city", city)
        if (!getLatLong(city)) {
            Toast.makeText(this, "Could not recognize city", Toast.LENGTH_LONG).show()
            finish()
        }
        Log.d("city", "$lat $long")
        // getRequest("http://api.openweathermap.org/data/2.5/weather?lat=55&lon=37&appid=ced99c1bbb8e1f79124ca31436495ae2")
        getRequest("http://api.openweathermap.org/data/2.5/forecast?lat=${lat.toInt()}&lon=${long.toInt()}&appid=$owmApiKey")
        // fillData("{\"coord\":{\"lon\":37,\"lat\":55},\"weather\":[{\"id\":802,\"main\":\"Clouds\",\"description\":\"scattered clouds\",\"icon\":\"03d\"}],\"base\":\"stations\",\"main\":{\"temp\":285.98,\"feels_like\":284.28,\"temp_min\":285.98,\"temp_max\":285.98,\"pressure\":1005,\"humidity\":37,\"sea_level\":1005,\"grnd_level\":983},\"visibility\":10000,\"wind\":{\"speed\":4.64,\"deg\":315,\"gust\":6.1},\"clouds\":{\"all\":35},\"dt\":1653303430,\"sys\":{\"country\":\"RU\",\"sunrise\":1653268320,\"sunset\":1653327946},\"timezone\":10800,\"id\":529315,\"name\":\"Marinki\",\"cod\":200}")
        // fillData("{\"coord\":{\"lon\":37,\"lat\":55},\"weather\":[{\"id\":802,\"main\":\"Clouds\",\"description\":\"scattered clouds\",\"icon\":\"03d\"}],\"base\":\"stations\",\"main\":{\"temp\":285.98,\"feels_like\":284.28,\"temp_min\":285.98,\"temp_max\":285.98,\"pressure\":1005,\"humidity\":37,\"sea_level\":1005,\"grnd_level\":983},\"visibility\":10000,\"wind\":{\"speed\":4.64,\"deg\":315,\"gust\":6.1},\"clouds\":{\"all\":35},\"dt\":1653303430,\"sys\":{\"country\":\"RU\",\"sunrise\":1653268320,\"sunset\":1653327946},\"timezone\":10800,\"id\":529315,\"name\":\"Marinki\",\"cod\":200}")
    }
    private val client = OkHttpClient()
    private fun getRequest(sUrl: String) {
        val request = Request.Builder()
            .url(sUrl)
            .build()

        // val logging = HttpLoggingInterceptor()
        // logging.level = (HttpLoggingInterceptor.Level.BASIC)
        // client.addInterceptor(logging)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("query", sUrl + " failure " + e.printStackTrace())}
            override fun onResponse(call: Call, response: Response) {
                var ans = (response.body?.string() ?: "")
                Log.d("query", ans)
                fillAllData(ans)
                response.close();
            }
        })
    }

    fun fillAllData(gotstr: String) {
        val jsonObject = JSONTokener(gotstr).nextValue() as JSONObject
        fillData(jsonObject.getJSONArray("list").getJSONObject(0),
            getString(R.string.today))
        fillData(jsonObject.getJSONArray("list").getJSONObject(1),
            getString(R.string.tomorrow))
        fillData(jsonObject.getJSONArray("list").getJSONObject(2),
            getString(R.string.ind) + " 2 " + getString(R.string.days))
        fillData(jsonObject.getJSONArray("list").getJSONObject(3),
            getString(R.string.ind) + " 3 " + getString(R.string.days))
        fillData(jsonObject.getJSONArray("list").getJSONObject(4),
            getString(R.string.ind) + " 4 " + getString(R.string.days))

    }

    fun fillData(jsonObject: JSONObject, day: String) {
        var forecast = Forecast()

        val iconId = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon")

        forecast.temp = String.format("%.2f", jsonObject.getJSONObject("main").getDouble("temp") - 273.15) + " °C"
        forecast.humidity = jsonObject.getJSONObject("main").getInt("humidity")
        forecast.wind = jsonObject.getJSONObject("wind").getDouble("speed")
        forecast.pressure = jsonObject.getJSONObject("main").getDouble("pressure")
        forecast.imageid = iconId
        forecast.day = day
        forecastslist.add(forecast)
        runOnUiThread {
            listAdapter.notifyDataSetChanged()
        }
    }

    fun getLatLong(city: String): Boolean{
        var coder = Geocoder(this)
        var address: List<Address>

        try {
            address = coder.getFromLocationName(city,5)
            if (address==null) {
                return false
            }
            var location: Address = address.get(0)
            lat = location.getLatitude()
            long = location.getLongitude()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}