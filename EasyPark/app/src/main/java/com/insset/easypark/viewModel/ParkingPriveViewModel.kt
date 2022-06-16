package com.insset.easypark.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Parking
import com.insset.easypark.model.ParkingPrive
import com.insset.easypark.repository.ParkingPriveeRepository

class ParkingPriveViewModel : ViewModel() {
    private val parkingPriveeRepository by lazy { ParkingPriveeRepository(FirestoreDataSource()) }
    var parkingPriveResultList = MutableLiveData<List<ParkingPrive>>()
    var parkingPriveList : MutableList<ParkingPrive> = mutableListOf()
    var parkingPriveId = ""


    fun validerParkingPrivee(parkingPrive : ParkingPrive) : String{
        if(parkingPrive.tarif == null){
            return("Veuillez indiquer le prix.")
        }
        else if(parkingPrive.adresse == null){
            return("Veuillez sélectionner une adresse.")
        }
        parkingPrive.parkingId = parkingPriveId

        parkingPriveeRepository.updateParkingPrivee(parkingPrive)


        return  "";
    }

    fun updateParkingPriveList(){
        parkingPriveList = mutableListOf()
        FirebaseFirestore.getInstance().collection("parkingPriveeParDepartement").get()
            .addOnSuccessListener { results ->
                for (result in results) {
                    FirebaseFirestore.getInstance()
                        .collection("parkingPriveeParDepartement")
                        .document(result.id)
                        .collection("parking").whereEqualTo("uid",
                            FirebaseAuth.getInstance().currentUser?.uid).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                parkingPriveList.add(document.toObject<ParkingPrive>())
                            }
                            parkingPriveResultList.value = parkingPriveList
                        } .addOnFailureListener { exception ->
                                Log.d("1", "Error getting documents: ", exception)
                            }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("1", "Error getting documents: ", exception)
            }
    }
}