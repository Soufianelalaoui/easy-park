package com.insset.easypark.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser
import com.insset.easypark.R
import com.insset.easypark.databinding.ActivityAdresseBinding
import com.insset.easypark.model.Adresse
import com.insset.easypark.viewModel.GetAdressesViewModel
import com.insset.easypark.viewModel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AdresseActivity : AppCompatActivity() {

    private lateinit var mViewModel: GetAdressesViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityAdresseBinding
    private lateinit var adapter : AdresseAdapter
    private val observer = Observer<List<Adresse>> {
        adapter.submitList(it)
    }
    private var mObserverUser = Observer<FirebaseUser> {
        if (it == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdresseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AdresseAdapter({ item -> onNavigationClick(item)},{ item -> deleteClick(item)} )

        mViewModel = ViewModelProvider(this)[GetAdressesViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.AdressesRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.AdressesRV.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        mViewModel.adresseResultList.observe(this, observer)
        mainViewModel.mCurrentUser.observe(this, mObserverUser)
    }


    override fun onStop() {
        mViewModel.adresseResultList.removeObserver(observer)
        super.onStop()
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

    private fun onNavigationClick(adresse: Adresse){
        val intent = Intent(this, RechercheParkingActivity::class.java)
        intent.putExtra("adresse", adresse)
        startActivity(intent)
    }

    private fun deleteClick(adresse: Adresse){
        mViewModel.deleteAdresse(adresse)
    }
}