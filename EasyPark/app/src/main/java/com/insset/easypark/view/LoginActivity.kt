package com.insset.easypark.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.insset.easypark.databinding.ActivityLoginBinding
import com.insset.easypark.viewModel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mViewModel: LoginViewModel

    private var mObserverUser = Observer<FirebaseUser> {
        if(it != null ){
            goToMainActivity()
        }
    }

    private var mObserverError = Observer<Int> {
        afficheError(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        switchToLogin()
    }


    fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun isEmptyFields(): Boolean {
        return TextUtils.isEmpty(binding.editTextEmailAddress.text.toString()) || TextUtils.isEmpty(binding.editTextPassword.text.toString())
    }

    @SuppressLint("SetTextI18n")
    fun switchToRegister() {
        binding.buttonLoginRegister.text = "S'enregistrer"
        binding.buttonLoginRegister.setOnClickListener { register() }
        binding.textViewSwitchLoginregister.text = "Se connecter"
        binding.textViewSwitchLoginregister.setOnClickListener { switchToLogin() }
    }

    @SuppressLint("SetTextI18n")
    fun switchToLogin() {
        binding.buttonLoginRegister.text = "Se connecter"
        binding.buttonLoginRegister.setOnClickListener { login() }
        binding.textViewSwitchLoginregister.text = "Créer un compte ?"
        binding.textViewSwitchLoginregister.setOnClickListener { switchToRegister() }
    }

    override fun onStart() {
        super.onStart()
        mViewModel.mCurrentUser.observe(this, mObserverUser)
        mViewModel.mErrorProcess.observe(this, mObserverError)
    }


    override fun onStop() {
        mViewModel.mCurrentUser.removeObserver(mObserverUser)
        mViewModel.mErrorProcess.removeObserver(mObserverError)
        super.onStop()
    }

    private fun login() {
        if (!isEmptyFields()) {
            mViewModel.loginUser(
                binding.editTextEmailAddress.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }else{
            Toast.makeText(this, "Email ou mdp vide !", Toast.LENGTH_SHORT).show()
        }
    }


    private fun register() {
        if (!isEmptyFields()) {
            mViewModel.registerNewUser(
                binding.editTextEmailAddress.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }else{
            Toast.makeText(this, "Email ou mdp vide !", Toast.LENGTH_SHORT).show()
        }
    }

    private fun afficheError(code: Int) {
        when (code) {
            5 -> Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
            9 -> Toast.makeText(this, "Utilisateur null!", Toast.LENGTH_SHORT).show()
            10 -> Toast.makeText(this, "Erreur lors de la création du profil", Toast.LENGTH_SHORT)
                .show()
            11 -> Toast.makeText(this, "Erreur lors de la connexion", Toast.LENGTH_SHORT).show()
        }
    }
}
