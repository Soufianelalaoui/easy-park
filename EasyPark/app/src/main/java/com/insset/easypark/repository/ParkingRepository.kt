package com.insset.easypark.repository

import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Parking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class ParkingRepository (
    private val firestoreDataSource: FirestoreDataSource,
) {
    @ExperimentalCoroutinesApi
    fun getParkingList(departement : String): Flow<List<Parking>> = firestoreDataSource.getParkingList(departement)

    fun saveParking(parking: Parking) = firestoreDataSource.saveParking(parking)
}