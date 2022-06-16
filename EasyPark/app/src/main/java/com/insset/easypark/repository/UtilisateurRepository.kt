package com.insset.easypark.repository

import com.insset.easypark.dataSource.FirestoreDataSource
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Utilisateur

class UtilisateurRepository (
    private val firestoreDataSource: FirestoreDataSource,
) {

    fun ajouteAdresse(adresse: Adresse) = firestoreDataSource.ajouteAdresse(adresse)
    fun deleteAdresse(adresse: Adresse) = firestoreDataSource.deleteAdresse(adresse)
    fun updateInfo(utilisateur: Utilisateur) = firestoreDataSource.updateUtilisateur(utilisateur)
}