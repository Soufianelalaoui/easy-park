package com.insset.easypark.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthRepository {
    private var mFirebaseAuth: FirebaseAuth = Firebase.auth
    var mCurrentUser = MutableLiveData<FirebaseUser>()
    var mErrorProcess = MutableLiveData<Int>()


    init {
        if(mFirebaseAuth.currentUser != null) {
            mCurrentUser.postValue(mFirebaseAuth.currentUser)
        }
    }

    fun registerNewUser(email: String, password: String) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (mFirebaseAuth.currentUser != null) {
                        mCurrentUser.postValue(mFirebaseAuth.currentUser)
                    } else {
                        mErrorProcess.postValue(9)
                    }
                } else {
                    mErrorProcess.postValue(10)
                }
            }
    }


    fun loginUser(email: String, password: String) {
        mErrorProcess.postValue(0)
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mCurrentUser.postValue(mFirebaseAuth.currentUser)
                } else {
                    mErrorProcess.postValue(11)
                }
            }
    }


    fun disconnectUser() {
        mFirebaseAuth.signOut()
        mCurrentUser.postValue(null)
        mErrorProcess.postValue(5)
    }



}