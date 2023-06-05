package com.example.coloria

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coloria.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

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
        setUserData()
        editPfp()

        //Buttons
        logoutButton()

        binding.bottomNavigation.selectedItemId = R.id.perfil

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
                    // Abre la actividad correspondiente para "Historial"
                    // ...
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FragmentHistory())
                        .commit()
                    true
                }
                R.id.perfil -> {
                    // Abre la actividad correspondiente para "Perfil"
                    // ...
                    true
                }
                else -> false
            }
        }
    }

    private fun editPfp() {
        val imageViewPfp = binding.imageViewPfp
        imageViewPfp.setOnClickListener{
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Cambiar foto de perfil")
            builder.setMessage("¿Quieres cambiar tu foto de perfil?")

            builder.setPositiveButton("Sí") { _, _ ->
                startImagePicker()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${email}") // Utiliza el email como nombre del archivo en Firebase Storage
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    if (downloadUri != null) {
                        val usersCollectionRef = db.collection("users").document(email.toString())
                        usersCollectionRef.update("pfp", downloadUri.toString())
                            .addOnSuccessListener {
                                setUserData()
                                // La URL de descarga de la imagen se guardó correctamente en Firestore
                                // Aquí puedes realizar cualquier operación adicional que necesites
                            }
                            .addOnFailureListener {
                                // Ocurrió un error al guardar la URL de descarga de la imagen en Firestore
                                // Maneja el error de acuerdo a tus necesidades
                            }
                    } else {
                        // No se pudo obtener la URL de descarga de la imagen desde Firebase Storage
                    }
                } else {
                    // Ocurrió un error al obtener la URL de descarga de la imagen desde Firebase Storage
                    // Maneja el error de acuerdo a tus necesidades
                }
            }
        }
    }

    private fun startImagePicker() {
        getContent.launch("image/*")
    }

    private fun setUserData() {
        val textViewName = binding.textViewName
        val textViewEmail = binding.textViewEmail
        val imageViewPfp = binding.imageViewPfp

        if (users != null) {
            getUserData().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val userName = documentSnapshot.getString("userName")
                    val email = documentSnapshot.getString("email")
                    val imageUrl = documentSnapshot.getString("pfp")

                    textViewName.text = userName
                    textViewEmail.text = email
                    if (imageUrl != null && imageUrl != ""){
                        Picasso.get().load(imageUrl).into(imageViewPfp)
                        //imageViewPfp.setImageURI(imageUrl.toUri())
                    }
                } else {
                    val text = "El nombre de usuario no se pudo obtener!"
                    showToast(text)
                    // El usuario no se encontró en la base de datos
                }
            }.addOnFailureListener {
                // Manejo de errores al obtener los datos de Firestore
                val text = "Errores al obtener los datos de la base de datos!"
                showToast(text)
            }
        } else {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
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

    private fun getUserData(): Task<QuerySnapshot> {
        val users = FirebaseAuth.getInstance().currentUser
        val email = users?.email

        val usersCollection = db.collection("users")
        val query = usersCollection.whereEqualTo("email", email)

        return query.get()
    }

    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }
}
