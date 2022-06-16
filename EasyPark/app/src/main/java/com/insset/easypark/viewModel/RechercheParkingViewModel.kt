package com.insset.easypark.viewModel

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Parking
import com.insset.easypark.model.ParkingHeader
import com.insset.easypark.model.ParkingRecyclerView
import com.insset.easypark.repository.ParkingRepository
import com.insset.easypark.repository.UtilisateurRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class RechercheParkingViewModel : ViewModel(){
    private var addresseDestination : Adresse? = null

    private var job : Job? = null
    var nbKm = 5
    private val parkingRepository: ParkingRepository by lazy { ParkingRepository(FirestoreDataSource()) }
    private val utilisateurRepository: UtilisateurRepository by lazy { UtilisateurRepository(FirestoreDataSource()) }
    var parkingList : MutableList<Parking>? = null
    var parkingResultList = MutableLiveData<List<ParkingRecyclerView>>()

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
                        parkingList?.removeIf { parking -> parking.id in parkingFlowList.map { parkingFlow -> parkingFlow.id } }
                        parkingList?.addAll(parkingFlowList)
                    }
                    filtreParkingResult()
                }
            }
        }
    }

    fun filtreParkingResult() {
        val result =  mutableListOf<Parking>()
        val locationAdresse = Location("")
        val locationParking = Location("")
        if(addresseDestination != null){
            locationAdresse.latitude = addresseDestination?.latitude!!
            locationAdresse.longitude = addresseDestination?.longitude!!
            parkingList?.forEach { parking ->
                locationParking.latitude = parking.latitude!!.toDouble()
                locationParking.longitude = parking.longitude!!.toDouble()
                parking.distanceDestination = locationAdresse.distanceTo(locationParking).toDouble()
                if( parking.distanceDestination!! <= nbKm  * 1000){
                    result.add(parking)
                }
            }
        }
        var sortedResult = mutableListOf<ParkingRecyclerView>()
        result.sortedBy { it.distanceDestination }.groupBy { it.etat == "OUVERT" }.forEach{ ( isOuvert, items) ->
            sortedResult.add(ParkingHeader((if (isOuvert) "Ouvert" else "Fermé")))
            sortedResult.addAll(items)

        }
        parkingResultList.value = sortedResult
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

    fun ajouteAdresse(libelle :String) {
        addresseDestination?.alias = libelle
        addresseDestination?.let {
            utilisateurRepository.ajouteAdresse(it) }
    }

    fun saveParking(parking: Parking){
        parkingRepository.saveParking(parking)
    }
}



