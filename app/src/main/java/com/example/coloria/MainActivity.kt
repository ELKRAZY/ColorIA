package com.example.coloria

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        bottomNavigationView = binding.bottomNavigation

        //UserData
        setUserData()
        editPfp()

        //Buttons
        logoutButton()


    }

    private fun editPfp(){
        val imageViewPfp = binding.imageViewPfp
        imageViewPfp.setOnClickListener{
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Cambiar foto de perfil")
            builder.setMessage("¿Quieres cambiar tu foto de perfil?")

            builder.setPositiveButton("Sí") { _, _ ->
                // Código para cambiar la foto de perfil aquí
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun setUserData() {
        val users = FirebaseAuth.getInstance().currentUser
        val textViewName = binding.textViewName
        val textViewEmail = binding.textViewEmail
        if (users != null) {
            val email = users.email

            val usersCollection = db.collection("users")
            val query = usersCollection.whereEqualTo("email", email)

            query.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val userName = documentSnapshot.getString("userName")
                    textViewName.text = userName
                    textViewEmail.text = email
                } else {
                    val text = "El nombre de usuario no se pudo obtener!"
                    showToast(text)
                    // El usuario no se encontró en la base de datos
                }
            }.addOnFailureListener {
                // Manejo de errores al obtener los datos de Firestore
                val text = "errores al obtener los datos de la base de datos!"
                showToast(text)
            }
        }
    }

    //Button method that cleans the shared preferences file and takes you back to the login (signOut)
    private fun logoutButton() {
        binding.buttonLogOut.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }



}