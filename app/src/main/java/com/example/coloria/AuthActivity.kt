package com.example.coloria

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import com.example.coloria.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("NAME_SHADOWING")
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar)

        //SetUp
        session()
        setup()

        //Recuperar contraseña
        passRecovery()

// Configurar el OnClickListener para el botón
        binding.buttonSignUp.setOnClickListener {
            // Crear un Intent para la actividad ActivitySignIn
            val intent = Intent(this, ActivitySignUp::class.java)
            // Iniciar la actividad ActivitySignIn
            startActivity(intent)
        }
// Establecer el contenido de la vista usando ViewBinding
        setContentView(binding.root)

    }


    private fun setup() {
        val editTextEmail = binding.editTextEmail
        val editTextPassword = binding.editTextPassword
        val buttonRemember = binding.checkBoxRemember



        binding.buttonLogGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)

            googleClient.signOut()
            signInLauncher.launch(googleClient.signInIntent)

        }

        binding.buttonSingIn.setOnClickListener {

            if (validateEmail(editTextEmail) && validatePassword(editTextPassword)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    editTextEmail.text.toString(),
                    editTextPassword.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {

                        if (buttonRemember.isChecked) {
                            val prefs = getSharedPreferences(
                                getString(R.string.prefs_file),
                                Context.MODE_PRIVATE
                            ).edit()
                            prefs.putString("email", editTextEmail.text.toString())
                            prefs.putString("password", editTextPassword.text.toString())
                            prefs.apply()

                        }
                        val intent = Intent(this, MainActivity::class.java)
                        val text = "Bienvenido!"
                        showToast(text)

                        // Iniciar la actividad ActivitySignIn
                        startActivity(intent)

                    } else {
                        val text = " La contraseña y/o el email no son correctos"
                        showToast(text)

                    }

                }
            } else {
                val text = "No se ha podido iniciar sesión X_x"
                showToast(text)

            }
        }
    }

    private fun showToast(text: String) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val gprefs = getSharedPreferences(getString(R.string.gprefs_file), Context.MODE_PRIVATE)

        val token = gprefs.getString("google_id_token", null)
        val email = prefs.getString("email", null)
        val password = prefs.getString("password", null)

        if (email != null && password != null || token != null) {
            val intent = Intent(this, MainActivity::class.java)
            val text = "Bienvenido!"
            showToast(text)

            // Iniciar la actividad ActivitySignIn
            startActivity(intent)
        }

    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // El inicio de sesión fue exitoso, puedes obtener el token de acceso aquí
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val token = task.getResult(ApiException::class.java)
                    // hacer algo con el token aquí
                    if (token != null) {
                        val credential = GoogleAuthProvider.getCredential(token.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val users = FirebaseAuth.getInstance().currentUser
                                    if (users != null) {
                                        val email = users.email
                                        val username = email?.substringBefore("@")
                                        val coll = db.collection("users").document(email.toString())
                                        coll.get().addOnSuccessListener { document ->
                                            if (document != null && document.exists()) {
                                                val email = document.getString("email")
                                                if (email != null) {
                                                    // El campo "email" se ha obtenido correctamente
                                                    val prefs = getSharedPreferences(
                                                        getString(R.string.gprefs_file),
                                                        Context.MODE_PRIVATE
                                                    ).edit()
                                                    prefs.putString(
                                                        "google_id_token",
                                                        token.idToken
                                                    )
                                                    prefs.apply()

                                                    val intent =
                                                        Intent(this, MainActivity::class.java)
                                                    val text = "Bienvenido!"
                                                    showToast(text)

                                                    // Iniciar la actividad ActivitySignIn
                                                    startActivity(intent)
                                                }
                                            } else {
                                                // El documento no existe
                                                val user = hashMapOf(
                                                    "userName" to username,
                                                    "email" to email,
                                                    "pfp" to ""
                                                )
                                                db.collection("users").document(email.toString())
                                                    .set(user)
                                                    .addOnCompleteListener { dbTask ->
                                                        if (dbTask.isSuccessful) {
                                                            val prefs = getSharedPreferences(
                                                                getString(R.string.gprefs_file),
                                                                Context.MODE_PRIVATE
                                                            ).edit()
                                                            prefs.putString(
                                                                "google_id_token",
                                                                token.idToken
                                                            )
                                                            prefs.apply()


                                                            db.collection("colorlist")
                                                                .document(email.toString())
                                                                .set(
                                                                    hashMapOf(
                                                                        "colorArrayList" to arrayListOf<String>(),
                                                                        "favColorList" to arrayListOf<String>()
                                                                    )
                                                                )


                                                            val intent = Intent(
                                                                this,
                                                                MainActivity::class.java
                                                            )
                                                            val text = "Bienvenido!"
                                                            showToast(text)

                                                            // Iniciar la actividad ActivitySignIn
                                                            startActivity(intent)
                                                        } else {
                                                            val text =
                                                                "Error al guardar el usuario en la base de datos!"
                                                            showToast(text)
                                                        }
                                                    }
                                            }
                                        }.addOnFailureListener {
                                            // Manejar la excepción, si ocurre
                                        }


                                    }
                                    // Iniciar la actividad ActivitySignIn
                                    startActivity(intent)
                                } else {
                                    val text = "Se ha producido un error al obtener el usuario!"
                                    showToast(text)
                                }
                            }
                    }

                } catch (e: ApiException) {
                    val text = "Se ha producido un error al obtener el usuario!"
                    showToast(text)

                }
            } else {
                val text = "Algo ha fallado!"
                showToast(text)
            }
        }

    private fun validateEmail(editTextEmail: EditText): Boolean {
        val email = editTextEmail.text.toString().trim()
        return if (email.isNotEmpty()) {
            true
        } else {
            val text = "¡Introduzca un email!"
            showToast(text)
            editTextEmail.error = "Introduzca un email"
            false
        }
    }

    private fun validatePassword(editTextPassword: EditText): Boolean {
        val password = editTextPassword.text.toString().trim()
        return if (password.isNotEmpty()) {
            true
        } else {
            val text = "¡Introduzca una contraseña!"
            showToast(text)
            editTextPassword.error = "Introduzca una contraseña"
            false
        }
    }

    private fun passRecovery() {

        binding.buttonPassRecovery.setOnClickListener {
            val editTextEmail = EditText(this)
            editTextEmail.hint = "Correo electrónico"

            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Recuperar contraseña")
                .setMessage("Ingrese su correo electrónico para recuperar su contraseña")
                .setView(editTextEmail)
                .setPositiveButton("Enviar") { dialog, _ ->
                    val email = editTextEmail.text.toString().trim()
                    if (email.isNotEmpty()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val text =
                                        "Se ha enviado un correo electrónico para restablecer la contraseña"
                                    showToast(text)
                                } else {
                                    val text =
                                        "No se pudo enviar el correo electrónico de recuperación de contraseña"
                                    showToast(text)
                                }
                            }
                    } else {
                        val text = "Por favor, ingrese su correo electrónico"
                        showToast(text)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            dialog.show()
        }

    }

}





