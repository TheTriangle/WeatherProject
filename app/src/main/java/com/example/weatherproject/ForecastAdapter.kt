package com.example.weatherproject

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class ForecastAdapter(gforecasts: ArrayList<Forecast>, context: Context?) :
    RecyclerView.Adapter<ForecastAdapter.ForecastHolder>() {
    // List to store all the contact details
    private var forecasts: ArrayList<Forecast>? = gforecasts
    private var mContext: Context? = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // Inflate the layout view you have created for the list rows here
        val view: View = layoutInflater.inflate(R.layout.item_weather_forecast, parent, false)
        return ForecastHolder(view)
    }

    override fun getItemCount(): Int {
        return forecasts?.size ?: 0
    }

    // This method is called when binding the data to the views being created in RecyclerView
    override fun onBindViewHolder(holder: ForecastHolder, position: Int) {
        val forecast: Forecast = forecasts!!.get(position)

        // Set the data to the views here
        holder.tvTemperature.setText(forecast.temp)
        holder.tvWind.setText(forecast.wind.toString())
        holder.tvPressure.setText(forecast.pressure.toString())
        holder.tvHumidity.setText(forecast.humidity.toString())
        holder.tvDay.setText(forecast.day)

        try {
            // val `is`: InputStream = URL("http://openweathermap.org/img/wn/$iconId@2x.png").content as InputStream
            // ivWeatherMain.setImageDrawable(Drawable.createFromStream(`is`, "src name"))
            val imageUri = "https://openweathermap.org/img/wn/${forecast.imageid}@2x.png"
            Log.d("image", "https://openweathermap.org/img/wn/${forecast.imageid}@2x.png")
            Picasso.with(mContext).load(imageUri).resize(400, 400).into(holder.ivWeatherMain)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    // This is your ViewHolder class that helps to populate data to the view
    inner class ForecastHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTemperature: TextView
        val tvWind: TextView
        val tvPressure: TextView
        val tvHumidity: TextView
        val ivWeatherMain: ImageView
        val tvDay: TextView

        init {
            tvTemperature = itemView.findViewById(R.id.tvTemperature)
            tvWind = itemView.findViewById(R.id.tvWind)
            tvPressure = itemView.findViewById(R.id.tvPressure)
            tvHumidity = itemView.findViewById(R.id.tvHumidity)
            ivWeatherMain = itemView.findViewById(R.id.ivWeatherMain)
            tvDay = itemView.findViewById(R.id.tvDay)
        }
    }
}