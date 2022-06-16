package com.insset.easypark.dataSource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Parking
import com.insset.easypark.model.ParkingPrive
import com.insset.easypark.model.Utilisateur
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreDataSource{
    // Method to get user events from the Firestore database
    @ExperimentalCoroutinesApi
    fun getParkingList(departement : String): Flow<List<Parking>> = callbackFlow {
        // Reference to use in Firestore
        var eventsCollection: CollectionReference? = null
        try {
            eventsCollection = FirebaseFirestore.getInstance()
                .collection("parkingParDepartement")
                .document(departement)
                .collection("parking")
        } catch (e: Throwable) {
            // If Firebase cannot be initialized, close the stream of data
            // flow consumers will stop collecting and the coroutine will resume
            close(e)
        }

        // Registers callback to firestore, which will be called on new events
        val subscription = eventsCollection?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) { return@addSnapshotListener }
            // Sends events to the flow! Consumers will get the new events
            try {
                this.trySend(snapshot.documentChanges.map{ documentChange -> documentChange.document.toObject(Parking::class.java) }).isSuccess
            } catch (e: Throwable) {
                Log.i("erreur", e.message.toString())
            }
        }
        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription?.remove() }
    }

    fun ajouteAdresse(adresse: Adresse){
        FirebaseFirestore.getInstance().collection("utilisateur").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("mesAdresses").document().set(adresse)
    }

    fun deleteAdresse(adresse : Adresse){
        FirebaseFirestore.getInstance().collection("utilisateur").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("mesAdresses").whereEqualTo("alias", adresse.alias).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }
    }


    fun saveParking(parking: Parking){
        FirebaseFirestore.getInstance().collection("utilisateur").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("dernierParking").document("parking").set(parking)
    }

    @ExperimentalCoroutinesApi
    fun getParkingPriveeList(departement : String): Flow<List<ParkingPrive>> = callbackFlow {
        // Reference to use in Firestore
        var eventsCollection: CollectionReference? = null
        try {
            eventsCollection = FirebaseFirestore.getInstance()
                .collection("parkingPriveeParDepartement")
                .document(departement)
                .collection("parking")
        } catch (e: Throwable) {
            // If Firebase cannot be initialized, close the stream of data
            // flow consumers will stop collecting and the coroutine will resume
            close(e)
        }

        // Registers callback to firestore, which will be called on new events
        val subscription = eventsCollection?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) { return@addSnapshotListener }
            // Sends events to the flow! Consumers will get the new events
            try {
                this.trySend(snapshot.documentChanges.map{ documentChange -> documentChange.document.toObject(ParkingPrive::class.java) }).isSuccess
            } catch (e: Throwable) {
                Log.i("erreur", e.message.toString())
            }
        }
        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription?.remove() }
    }


    fun updateParkingPrivee(parkingPrive: ParkingPrive){
        var parkingId : String
        if(!parkingPrive.parkingId.isNullOrEmpty()){
            parkingId= parkingPrive.parkingId.toString()
        }else{
            parkingId = FirebaseFirestore.getInstance().collection("parkingPriveeParDepartement")
                .document(parkingPrive.adresse?.postalCode.toString())
                .collection("parking").document().id
            parkingPrive.parkingId = parkingId
        }
        parkingPrive.parkingId?.let {
            FirebaseFirestore.getInstance().collection("parkingPriveeParDepartement")
                .document(parkingPrive.adresse?.postalCode.toString()).set(mapOf("cp" to parkingPrive.adresse?.postalCode.toString())  )
            FirebaseFirestore.getInstance().collection("parkingPriveeParDepartement")
                .document(parkingPrive.adresse?.postalCode.toString()).collection("parking")
                .document(it)
                .set(parkingPrive)
        }
    }

    fun deleteParkingPrivee(parkingPrive: ParkingPrive){
        parkingPrive.parkingId?.let {
            FirebaseFirestore.getInstance().collection("parkingPriveeParDepartement")
                .document(parkingPrive.adresse?.postalCode.toString())
                .collection("parking")
                .document(it)
                .delete()
        }
    }

    fun updateUtilisateur(utilisateur: Utilisateur){
        FirebaseFirestore.getInstance()
            .collection("utilisateur")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .set(utilisateur)

    }

}