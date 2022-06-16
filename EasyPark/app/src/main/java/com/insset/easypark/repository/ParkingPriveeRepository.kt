package com.insset.easypark.repository

import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.ParkingPrive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class ParkingPriveeRepository(private val firestoreDataSource: FirestoreDataSource,) {

    @ExperimentalCoroutinesApi
    fun getParkingList(departement : String): Flow<List<ParkingPrive>> = firestoreDataSource.getParkingPriveeList(departement)

    fun updateParkingPrivee(parkingPrive: ParkingPrive) = firestoreDataSource.updateParkingPrivee(parkingPrive)

    fun deleteParkingPrivee(parkingPrive: ParkingPrive) = firestoreDataSource.deleteParkingPrivee(parkingPrive)
}