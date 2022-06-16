package com.insset.easypark.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.location.Geocoder
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.insset.easypark.R
import com.insset.easypark.databinding.ActivityMainBinding
import com.insset.easypark.databinding.MesInfosDialogBinding
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.LocationDetails
import com.insset.easypark.model.Parking
import com.insset.easypark.viewModel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
private const val TAG = "BroadcastReceiver"

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mViewModel: MainViewModel
    private lateinit var geocoder : Geocoder
    private lateinit var dialogBinding: MesInfosDialogBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private var myLocation: LocationDetails? = null;
    private lateinit var dialog : Dialog


    @SuppressLint("SetTextI18n")
    private var mObserverUser = Observer<FirebaseUser> {
        if (it == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            binding.user.text = "Utilisateur : " + it.email
        }
    }

    private var mObserverLocation = Observer<LocationDetails>{
        if(it != null){
            myLocation = it
            binding.buttonParkingAproximite.visibility = View.VISIBLE
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        mViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(view)
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        dialogBinding = MesInfosDialogBinding.inflate(layoutInflater)
        dialogBinding.validation.setOnClickListener { validaterUpdateInfo() }
        dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        //Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Notification", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        binding.boutonParkingProcheDestination.setOnClickListener { goToParkinkDestination() }
        binding.buttonMesAdresses.setOnClickListener { goToMyAdresses() }
        binding.buttonLocaliserMaVoiture.setOnClickListener { goToLastParking() }
        binding.buttonParkingAproximite.setOnClickListener { getParkingPrximite() }
        binding.buttonParkingPrive.setOnClickListener{ goToParkingPrivee() }
        binding.buttonRechercheParkingPrive.setOnClickListener { goToRechercheParkingPrive() }
        binding.buttonUpdateInfo.setOnClickListener { openInfoDialog() }
        binding.deconnexionButton.setOnClickListener { signOut() }
        mViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(view)
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


    override fun onStart() {
        super.onStart()
        prepRequestLocationUpdate()
        mViewModel.mCurrentUser.observe(this, mObserverUser)
    }

    fun signOut() {
        mViewModel.disconnectUser()
    }

    override fun onStop() {
        super.onStop()
        mViewModel.locationLiveData.removeObserver(mObserverLocation)
        mViewModel.mCurrentUser.removeObserver(mObserverUser)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    requestLocationUpdates()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    private fun prepRequestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestLocationUpdates() {
        mViewModel.locationLiveData.observe(this, mObserverLocation)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun goToParkinkDestination() {
        val intent = Intent(this, RechercheParkingActivity::class.java)
        startActivity(intent)
    }

    fun goToMyAdresses() {
        val intent = Intent(this, AdresseActivity::class.java)
        startActivity(intent)
    }

    fun goToLastParking() {
        FirebaseFirestore.getInstance().collection("utilisateur").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("dernierParking").document("parking").get().addOnSuccessListener { document ->
                val parking = document.toObject<Parking>()
                val gmmIntentUri = Uri.parse("geo:${parking?.latitude},${parking?.longitude}?q=${parking?.latitude},${parking?.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }

    }

    fun getParkingPrximite(){
        val monAdresse = geocoder.getFromLocation(myLocation!!.latitude.toDouble(),myLocation!!.longitude.toDouble(),1).get(0)
        val adresse = Adresse("ma position","",monAdresse.postalCode.substring(0,2), myLocation!!.latitude.toDouble(), myLocation!!.longitude.toDouble())
        val intent = Intent(this, RechercheParkingActivity::class.java)
        intent.putExtra("adresse", adresse)
        startActivity(intent)
    }

    fun goToParkingPrivee(){
        if(mViewModel.utilisateur?.uid == null){
            dialog.show()
            Toast.makeText(this, "Veillez renseigner vos informations avant.", Toast.LENGTH_SHORT).show()

        }else{
            val intent = Intent(this, ParkingPriveActivity::class.java)
            startActivity(intent)
        }
    }

    fun goToRechercheParkingPrive(){
        val intent = Intent(this, RechercheParkingPriveActivity::class.java)
        startActivity(intent)
    }

    fun validaterUpdateInfo(){
        if(dialogBinding.editTextTextEmailAddress.text.isNullOrEmpty()){
            Toast.makeText(this, "Veillez renseigner l'email", Toast.LENGTH_SHORT).show()
        }
        else if(dialogBinding.editTextTextPersonName.text.isNullOrEmpty()){
            Toast.makeText(this, "Veillez renseigner un nom", Toast.LENGTH_SHORT).show()
        }else{
            mViewModel.updateInfo(dialogBinding.editTextTextPersonName.text.toString(),dialogBinding.editTextTextEmailAddress.text.toString(), dialogBinding.editTextPhone.text.toString())
            dialog.hide()
        }
    }

    fun openInfoDialog(){
        dialogBinding.editTextTextPersonName.setText(mViewModel.utilisateur?.nom)
        dialogBinding.editTextTextEmailAddress.setText(mViewModel.utilisateur?.email)
        dialogBinding.editTextPhone.setText(mViewModel.utilisateur?.tel)
        dialog.show()
    }



}