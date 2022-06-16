package com.insset.easypark.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Adresse(
    val libelle: String? ="",
    var alias: String? ="",
    val postalCode : String? ="",
    val latitude: Double? =0.0,
    val longitude: Double? =0.0
    ) : Parcelable