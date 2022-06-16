package com.insset.easypark.view

import android.app.Dialog
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.insset.easypark.R

import com.insset.easypark.databinding.ActivityParkingPriveBinding
import com.insset.easypark.databinding.ParkingPriveFormulaireDialogBinding
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.ParkingNoResult
import com.insset.easypark.model.ParkingPrive
import com.insset.easypark.model.ParkingRecyclerView
import com.insset.easypark.viewModel.MainViewModel
import com.insset.easypark.viewModel.ParkingPriveViewModel
import java.lang.Exception

class ParkingPriveActivity : AppCompatActivity(){
    private lateinit var mViewModel: ParkingPriveViewModel
    private lateinit var binding: ActivityParkingPriveBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var dialogBinding: ParkingPriveFormulaireDialogBinding
    private lateinit var geocoder : Geocoder
    private var adresse : Adresse? = null
    private lateinit var dialog : Dialog
    private lateinit var adapter : MParkingPriveAdapter

    private var mObserverParkingList = Observer<List<ParkingPrive>> {
        adapter.submitList(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)
        binding = ActivityParkingPriveBinding.inflate(layoutInflater)
        dialogBinding = ParkingPriveFormulaireDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mViewModel = ViewModelProvider(this)[ParkingPriveViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.addParkingPriveeButton.setOnClickListener {  showParkingPrivedialog() }
        dialogBinding.button.setOnClickListener { validerParkingPriveDialog() }

        Places.initialize(applicationContext, "AIzaSyBUVczmtuLSw_1XtdLQ07B1Re-B_kQXfKM")
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_parkingPrive)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setCountry("FR")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val nouvelleAddresse = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1).get(0)
                adresse = Adresse(place.name + " " + nouvelleAddresse.locality,null,nouvelleAddresse.postalCode?.toString()?.substring(0,2), place.latLng.latitude, place.latLng.longitude)
            }

            override fun onError(status: Status) {
                Log.i("test", "An error occurred: $status")
            }
        })

        dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        adapter = MParkingPriveAdapter({ item -> onUpdateClick(item)})
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        mViewModel.parkingPriveResultList.observe(this, mObserverParkingList)
        mViewModel.updateParkingPriveList()
    }

    override fun onStop() {
        super.onStop()
        mViewModel.parkingPriveResultList.removeObserver(mObserverParkingList)
    }

    fun showParkingPrivedialog(){
        dialogBinding.fragment.visibility = View.VISIBLE
        dialog.show()
    }

    fun validerParkingPriveDialog(){
        val tarif : Int
        try{
            tarif = Integer.parseInt(dialogBinding.editTextNumber.text.toString())
        }catch (exception : Exception){
            Toast.makeText(this, "Tarif n'est pas un nombre", Toast.LENGTH_SHORT).show()
            return
        }
        var parkingPrive = ParkingPrive("",
            adresse,
            dialogBinding.switchParkingDispo.isChecked,
            dialogBinding.switchParkingInterieur.isChecked,
            dialogBinding.switchParkingSecurise.isChecked,
            tarif,
            null,
            FirebaseAuth.getInstance().uid.toString())

        var erreur = mViewModel.validerParkingPrivee(parkingPrive)
        if(erreur.isNotEmpty()){
            Toast.makeText(this, erreur, Toast.LENGTH_SHORT).show()
        }else{
            dialog.hide()
        }
        mViewModel.updateParkingPriveList()

    }

    fun onUpdateClick( parkingPrive: ParkingPrive){
        mViewModel.parkingPriveId = parkingPrive.parkingId.toString()
        dialogBinding.fragment.visibility = View.GONE
        adresse = parkingPrive.adresse
        parkingPrive.tarif?.let { dialogBinding.editTextNumber.setText(it.toString()) }
        dialogBinding.switchParkingDispo.isChecked = parkingPrive.disponibilite
        dialogBinding.switchParkingInterieur.isChecked = parkingPrive.interieur
        dialogBinding.switchParkingSecurise.isChecked = parkingPrive.securise
        dialog.show()
    }

}