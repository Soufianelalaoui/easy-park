package com.insset.easypark.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationLiveData (context: Context): LiveData<LocationDetails> (){

    private var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    companion object{
        val locationRequest : LocationRequest = LocationRequest.create().apply {
            interval = 60000
            fastestInterval = 60000/4
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onInactive()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location :Location  -> location.also {
                setLocationData(it)
            }
        }
        startLocationUpdates()
    }

    private val locationCallBack = object :LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult ?: return
            for(location in locationResult.locations){
                setLocationData(location)
            }
        }
    }

    private fun setLocationData(location: Location) {
        value = LocationDetails(location.longitude.toString(), location.latitude.toString())
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.getMainLooper())
    }
}

data class LocationDetails(
    val longitude : String,
    val latitude:String)
