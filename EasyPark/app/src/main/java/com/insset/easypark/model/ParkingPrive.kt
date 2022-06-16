package com.insset.easypark.model

data class ParkingPrive(
    var parkingId: String? = "",
    val adresse: Adresse? = null,
    val disponibilite: Boolean = false,
    val interieur: Boolean = false,
    val securise : Boolean = false,
    val tarif: Int? = 0,
    var distanceDestination : Double? =0.0,
    val uid: String? = ""
) : ParkingRecyclerView() {}