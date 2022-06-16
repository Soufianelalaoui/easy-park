package com.insset.easypark.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.Adresse
import com.insset.easypark.repository.UtilisateurRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class GetAdressesViewModel: ViewModel() {
    var adresseResultList = MutableLiveData<List<Adresse>>()
    private val utilisateurRepository: UtilisateurRepository by lazy { UtilisateurRepository(FirestoreDataSource()) }

    init {
        adresseResultList = MutableLiveData<List<Adresse>>()
        recupAdressesList()
    }

    fun recupAdressesList() {
        FirebaseFirestore.getInstance().collection("utilisateur").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("mesAdresses").get()
            .addOnSuccessListener { result ->
                val adressesList = mutableListOf<Adresse>()
                for (document in result) {
                    adressesList.add(document.toObject<Adresse>())
                }
                adresseResultList.value = adressesList
            }
            .addOnFailureListener { exception ->
                Log.d("1", "Error getting documents: ", exception)
            }
    }
    fun deleteAdresse(adresse: Adresse){
        utilisateurRepository.deleteAdresse(adresse)
        val adresses = adresseResultList.value?.toMutableList()
        adresses?.remove(adresse)
        adresseResultList.value = adresses
    }
}
