package com.insset.easypark.model

import java.util.*

sealed class ParkingRecyclerView

data class Parking (val nom : String? ="",
                    val etat : String? ="",
                    val bailles : String? = (50 until 90).random().toString(),
                    val ville : String? ="",
                    val adresse : String? ="",
                    val latitude : String? ="",
                    val longitude : String? ="",
                    val datemaj : String? ="",
                    var distanceDestination : Double? =0.0,
                    val placeTotal : Long? =0,
                    val placeDispo : Long? =0,
                    val id : String? =""
) : ParkingRecyclerView()

data class ParkingHeader (val libelle : String) : ParkingRecyclerView()

data class ParkingNoResult (val libelle : String) : ParkingRecyclerView()