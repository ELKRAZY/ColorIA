package com.example.coloria

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coloria.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private lateinit var bottomNavigationView: BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var binding: ActivityMainBinding
    private val users = FirebaseAuth.getInstance().currentUser
    private lateinit var imageUri: Uri
    private val email = users?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        bottomNavigationView = binding.bottomNavigation

        //UserData
//        setUserData()
//        editPfp()
//
//        //Buttons
//        logoutButton()


        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.foto -> {
                    // Abre la clase CameraActivity
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.imagina -> {
                    // Abre la actividad correspondiente para "Imagina"
                    // ...
                    true
                }
                R.id.favoritos -> {
                    // Abre la actividad correspondiente para "Favoritos"
                    // ...
                    true
                }
                R.id.historial -> {
                    // Reemplaza el fragmento principal por el FragmentHistory
                    val fragment = FragmentHistory()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit()
                    true
                }
                R.id.perfil -> {
                    // Abre la actividad correspondiente para "Perfil"
                    // ...
                    val fragment = MainFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit()

                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.perfil

    }


}
