package com.example.weatherproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*


class CitySelectActivity : AppCompatActivity() {

    // initializing
    // FusedLocationProviderClient
    // object
    var mFusedLocationClient: FusedLocationProviderClient? = null

    var PERMISSION_ID = 44
    lateinit var actvCities: AutoCompleteTextView
    lateinit var btnNext: Button
    lateinit var cities: Array<String?>
    lateinit var btnChangeLocale: Button
    lateinit var btnChangeTheme: Button
    lateinit var starterIntent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        if (useAlternativeTheme) {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Light, true)
        } else {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Dark, true)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cityselect)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        actvCities = findViewById(R.id.actvCities)
        fillCities()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        btnNext = findViewById(R.id.btnNext)
        btnNext.setOnClickListener { nextActivity() }
        btnChangeLocale = findViewById(R.id.btnChangeLanguage)
        btnChangeLocale.setOnClickListener { changeLocale() }
        btnChangeTheme = findViewById(R.id.btnSwitchTheme)
        btnChangeTheme.setOnClickListener { switchTheme() }
        if (useAlternativeTheme) {
            btnChangeTheme.setText(getString(R.string.lighttheme))
        } else {
            btnChangeTheme.setText(getString(R.string.darktheme))
        }
        fillCurrentCity()
        starterIntent = getIntent()
    }
    companion object {
        var useAlternativeTheme = true
    }
    fun switchTheme() {
        Log.d("Theme", useAlternativeTheme.toString())
        if (useAlternativeTheme) {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Light, true)
            btnChangeTheme.setText(getString(R.string.lighttheme))
        } else {
            theme.applyStyle(com.google.android.material.R.style.Base_Theme_Material3_Dark, true)
            btnChangeTheme.setText(getString(R.string.darktheme))
        }
        useAlternativeTheme = !useAlternativeTheme
        // invalidateOptionsMenu()
        recreate()
    }

    fun nextActivity() {
        var found = false
        val city = actvCities.text.toString()
        val intent = Intent(this, WeatherDisplayActivity::class.java).apply {
            putExtra("city", city.split("\t")[0])
        }
        startActivity(intent)
    }

    fun changeLocale() {
        val current = resources.configuration.locale
        var locale = Locale.getDefault()
        if (current == Locale.getDefault()) {
            locale = Locale("ru")
        }
        val res: Resources = resources
        val dm: DisplayMetrics = res.getDisplayMetrics()
        val conf: Configuration = res.getConfiguration()
        conf.locale = locale
        res.updateConfiguration(conf, dm)
        baseContext.resources.updateConfiguration(conf, baseContext.resources.displayMetrics)
        invalidateOptionsMenu()
        recreate()
    }

    fun fillCities() {
        var resources: Resources
        resources = getResources()
        cities = resources.getStringArray(R.array.cities)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, cities
        )
        actvCities.setAdapter(adapter)

    }
    var fusedLocationClient: FusedLocationProviderClient? = null

    fun fillCurrentCity() {
        getLastLocation()
    }

    fun fillCurentCity(lat: Double, long: Double) {
        // val aLocale: Locale = Locale.Builder().setLanguage("en").setScript("Latn").setRegion("RS").build()
        val current = resources.configuration.locale
        val geocoder = Geocoder(this, Locale.US)
        val addresses: List<Address> = geocoder.getFromLocation(lat,
            long, 1)

        val cityName: String = addresses[0].getAddressLine(0)
        // val stateName: String = addresses[0].getAddressLine(1)
        // val countryName: String = addresses[0].getAddressLine(2)
        if (addresses.get(0).locality.equals("Moskva"))
            actvCities.setText("Moscow")
        else
            actvCities.setText(addresses.get(0).locality)

        Log.d("location", cityName + " " )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        fillCurentCity(location.latitude, location.longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            fillCurentCity(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastLocation()
        }
    }
}