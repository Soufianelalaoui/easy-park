package com.insset.easypark.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseUser
import com.insset.easypark.R
import com.insset.easypark.databinding.*
import com.insset.easypark.model.*
import com.insset.easypark.receiver.NotificationBroadcastReceiver
import com.insset.easypark.viewModel.MainViewModel
import com.insset.easypark.viewModel.RechercheParkingPriveViewModel
import com.insset.easypark.viewModel.RechercheParkingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class RechercheParkingPriveActivity : AppCompatActivity() {

    private lateinit var mViewModel: RechercheParkingPriveViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityRechercheParkingPriveBinding
    private lateinit var dialogBinding: ChoixContactDialogBinding

    private lateinit var adapter : RechercheParkingPriveAdapter
    private lateinit var geocoder : Geocoder
    private lateinit var layoutBinding : EditTextLayoutBinding
    private lateinit var dialog : Dialog
    private lateinit var utilisateur : Utilisateur
    private lateinit var parkingPrive: ParkingPrive


    private var mObserverParkingList = Observer<List<ParkingRecyclerView>> {
        if(it.size > 0){
            adapter.submitList(it)
        }else{
            val list = mutableListOf(ParkingNoResult(""))
            adapter.submitList(list as List<ParkingRecyclerView>?)
        }

    }

    private var mObserverUser = Observer<FirebaseUser> {
        if (it == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private var mObserverParkingUtilisateur = Observer<Utilisateur> {
        dialogBinding.nomContact.text = "Contacter " + it.nom
        dialogBinding.buttonTelephone.visibility = if(it.tel.isNullOrEmpty()) View.GONE else View.VISIBLE
        dialog.show()
        utilisateur = it

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)
        mViewModel = ViewModelProvider(this)[RechercheParkingPriveViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = ActivityRechercheParkingPriveBinding.inflate(layoutInflater)
        layoutBinding = EditTextLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        adapter = RechercheParkingPriveAdapter({ item -> onNavigationClick(item)}, { item -> onContactClick(item)})
        dialogBinding = ChoixContactDialogBinding.inflate(layoutInflater)
        dialogBinding.buttonMail.setOnClickListener { sendEmail() }
        dialogBinding.buttonTelephone.setOnClickListener { checkPermission() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.seekBar2.progress = mViewModel.nbKm
        binding.nbKm.text = "Périmètre : " + mViewModel.nbKm.toString() + "Km"
        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                binding.nbKm.text = "Périmètre : " +(progress + 1).toString() + "Km"
                mViewModel.nbKm = progress + 1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mViewModel.filtreParkingResult()
            }
        })
        setContentView(view)
        dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        // Initialize the SDK
        Places.initialize(applicationContext, "AIzaSyBUVczmtuLSw_1XtdLQ07B1Re-B_kQXfKM")
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setCountry("FR")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME,Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val nouvelleAddresse = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1).get(0)
                mViewModel.changeAdresse(Adresse(place.name,null,nouvelleAddresse.postalCode.toString().substring(0,2), place.latLng.latitude, place.latLng.longitude))
            }

            override fun onError(status: Status) {
                Log.i("test", "An error occurred: $status")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val adresse = intent.extras?.get("adresse")
        if(adresse != null){
            mViewModel.changeAdresse(adresse as Adresse)
            intent.removeExtra("adresse")
        }
        mViewModel.parkingResultList.observe(this, mObserverParkingList)
        mainViewModel.mCurrentUser.observe(this, mObserverUser)
        mViewModel.parkingUtilisateur.observe(this, mObserverParkingUtilisateur)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.parkingResultList.removeObserver(mObserverParkingList)
        mainViewModel.mCurrentUser.removeObserver(mObserverUser)
        mViewModel.parkingUtilisateur.removeObserver(mObserverParkingUtilisateur)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.buttonDeconnexion -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun signOut() {
        mainViewModel.disconnectUser()
    }

    fun onNavigationClick(parking: ParkingRecyclerView){
        if (parking is ParkingPrive){
            val gmmIntentUri = Uri.parse("geo:${parking.adresse?.latitude},${parking.adresse?.longitude}?q=${parking.adresse?.latitude},${parking.adresse?.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
            startActivity(mapIntent)
        }
    }

    fun onContactClick(parking: ParkingRecyclerView){
        if (parking is ParkingPrive){
            parkingPrive = parking
            mViewModel.getUserParkingInfo(parking)
        }
    }

    private fun sendEmail() {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(utilisateur.email))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "location parking " + parkingPrive.adresse?.libelle)
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, "Bonjour je souhaiterais louer votre parking...")
        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42)
            }
        } else {
            // Permission has already been granted
            callPhone()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay!
                callPhone()
            } else {
                // permission denied, boo! Disable the
                // functionality
            }
            return
        }
    }

    fun callPhone(){
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + utilisateur.tel))
        startActivity(intent)
    }
}