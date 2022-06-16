package com.insset.easypark.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.LocationLiveData
import com.insset.easypark.model.Utilisateur
import com.insset.easypark.repository.FirebaseAuthRepository
import com.insset.easypark.repository.UtilisateurRepository

class MainViewModel(application: Application) : AndroidViewModel(application){
    val locationLiveData= LocationLiveData(application)
    var utilisateur : Utilisateur? = null;
    private val mFirebaseAuthRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
    private val utilisateurRepository : UtilisateurRepository by lazy { UtilisateurRepository(FirestoreDataSource())}
    var mCurrentUser = MutableLiveData<FirebaseUser>()
    init {
        mCurrentUser = mFirebaseAuthRepository.mCurrentUser
        FirebaseFirestore.getInstance()
            .collection("utilisateur")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .get().addOnSuccessListener { document ->
                if(document!=null){
                    utilisateur = document.toObject<Utilisateur>()
                }
            }
    }

    fun disconnectUser() {
        mFirebaseAuthRepository.disconnectUser()
    }

    fun updateInfo(nom : String, email: String, phone :String){
        FirebaseAuth.getInstance().currentUser?.let {
            Utilisateur(
                it.uid,email, nom, phone)
        }?.let {
            utilisateurRepository.updateInfo(it)
            utilisateur = it
        }
    }

}