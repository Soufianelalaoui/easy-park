package com.insset.easypark.viewModel

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.*
import com.insset.easypark.repository.ParkingPriveeRepository
import com.insset.easypark.repository.ParkingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class RechercheParkingPriveViewModel : ViewModel(){
    private var addresseDestination : Adresse? = null

    private var job : Job? = null
    var nbKm = 5
    private val parkingRepository: ParkingPriveeRepository by lazy { ParkingPriveeRepository(FirestoreDataSource()) }
    var parkingList : MutableList<ParkingPrive>? = null
    var parkingResultList = MutableLiveData<List<ParkingRecyclerView>>()
    var parkingUtilisateur = MutableLiveData<Utilisateur>()

    private fun changeDepartementRecherche(departement : String){
        job?.cancel()
        job = viewModelScope.launch {
            parkingRepository.getParkingList(departement)
            .catch {
                    exception -> Log.i("flow error : " , exception.message.toString())
            }
            .collect {
                parkingFlowList -> run {
                    if (parkingList == null) {
                        parkingList = parkingFlowList.toMutableList()
                    } else {
                        parkingList?.removeIf { parking -> parking.parkingId in parkingFlowList.map { parkingFlow -> parkingFlow.parkingId } }
                        parkingList?.addAll(parkingFlowList)
                    }
                    filtreParkingResult()
                }
            }
        }
    }

    fun filtreParkingResult() {
        val result =  mutableListOf<ParkingPrive>()
        val locationAdresse = Location("")
        val locationParking = Location("")
        if(addresseDestination != null){
            locationAdresse.latitude = addresseDestination?.latitude!!
            locationAdresse.longitude = addresseDestination?.longitude!!
            parkingList?.forEach { parking ->
                locationParking.latitude = parking.adresse?.latitude!!.toDouble()
                locationParking.longitude = parking.adresse.longitude!!.toDouble()
                parking.distanceDestination = locationAdresse.distanceTo(locationParking).toDouble()
                if( parking.distanceDestination!! <= nbKm  * 1000){
                    result.add(parking)
                }
            }
        }
        parkingResultList.value = result.sortedBy{ it.distanceDestination }
    }

    fun changeAdresse(adresse: Adresse){

        if (addresseDestination == null || adresse.postalCode.equals(addresseDestination!!.postalCode)){
            this.addresseDestination= adresse
            this.parkingList = null
            changeDepartementRecherche(adresse.postalCode.toString())
        }else{
            this.addresseDestination= adresse
            filtreParkingResult()
        }
    }

    fun getUserParkingInfo(parkingPrive: ParkingPrive){
        FirebaseFirestore.getInstance()
            .collection("utilisateur")
            .document(parkingPrive.uid.toString()).get()
            .addOnSuccessListener { document ->
                if(document != null){
                    parkingUtilisateur.value = document.toObject<Utilisateur>()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("1", "Error getting documents: ", exception)
            }

    }
}



