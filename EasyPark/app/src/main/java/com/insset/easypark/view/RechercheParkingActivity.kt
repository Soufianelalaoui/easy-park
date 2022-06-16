package com.insset.easypark.view

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.insset.easypark.databinding.ActivityRechercheParkingBinding
import com.insset.easypark.databinding.EditTextLayoutBinding
import com.insset.easypark.model.Adresse
import com.insset.easypark.model.Parking
import com.insset.easypark.model.ParkingNoResult
import com.insset.easypark.model.ParkingRecyclerView
import com.insset.easypark.receiver.NotificationBroadcastReceiver
import com.insset.easypark.viewModel.MainViewModel
import com.insset.easypark.viewModel.RechercheParkingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class RechercheParkingActivity : AppCompatActivity() {

    private lateinit var mViewModel: RechercheParkingViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityRechercheParkingBinding
    private lateinit var adapter : RechercheParkingAdapter
    private lateinit var geocoder : Geocoder
    private lateinit var layoutBinding : EditTextLayoutBinding

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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geocoder = Geocoder(this)
        mViewModel = ViewModelProvider(this)[RechercheParkingViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = ActivityRechercheParkingBinding.inflate(layoutInflater)
        layoutBinding = EditTextLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        adapter = RechercheParkingAdapter ({ item -> onNavigationClick(item)})

        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.floatingActionButton4.setOnClickListener { onAddAdresseClick() }
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
                binding.floatingActionButton4.visibility = View.VISIBLE
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
            binding.floatingActionButton4.visibility = View.GONE
        }
        mViewModel.parkingResultList.observe(this, mObserverParkingList)
        mainViewModel.mCurrentUser.observe(this, mObserverUser)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.parkingResultList.removeObserver(mObserverParkingList)
        mainViewModel.mCurrentUser.removeObserver(mObserverUser)
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

    private fun onNavigationClick(parking: ParkingRecyclerView){
        if (parking is Parking){
            mViewModel.saveParking(parking)
            val gmmIntentUri = Uri.parse("geo:${parking.latitude},${parking.longitude}?q=${parking.latitude},${parking.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            parking.id?.let { sendNotification(it) }
            startActivity(mapIntent)
        }
    }

    fun onAddAdresseClick(){
        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle("Ajouter à mes adresses")
            setPositiveButton("Ok"){dialog, wich ->
                val activity = ((dialog as AlertDialog).ownerActivity as RechercheParkingActivity)
                activity.mViewModel.ajouteAdresse(layoutBinding.etlibelle.text.toString())
                activity.binding.floatingActionButton4.visibility = View.GONE
                dialog.dismiss()
            }
            setNegativeButton("Annuler"){dialog, wich ->
                dialog.cancel()
                Log.d("Main", "Clique sur Annuler")
            }
            setView(layoutBinding.root)
        }
        val dialog = builder.create()
        dialog.setOwnerActivity(this)
        dialog.show()
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(parkingId: String){


        val yesIntent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
            action = "yes"
        }
        yesIntent.putExtra("parkingCode", parkingId)
        val pendingIntentYes = PendingIntent.getBroadcast(this, 1, yesIntent, PendingIntent.FLAG_IMMUTABLE)


        val noIntent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
            action = "no"
        }
        noIntent.putExtra("parkingCode", parkingId)
        val pendingIntentNo = PendingIntent.getBroadcast(this, 2, noIntent, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(this, "Notification")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText("Avez vous trouvé une place ?")
            .setContentTitle("De la place ?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(1, "Yes", pendingIntentYes)
            .addAction(2, "No", pendingIntentNo)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

}