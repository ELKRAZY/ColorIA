package com.example.coloria

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import com.example.coloria.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val _googleSingIn = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar)

        //SetUp
        session()
        setup()

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

    private fun setup(){
        val editTextEmail = binding.editTextEmail
        val editTextPassword = binding.editTextPassword
        val buttonRemember = binding.checkBoxRemember

        val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == _googleSingIn) {
                // El inicio de sesión fue exitoso, puedes obtener el token de acceso aquí
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    val token = task.getResult(ApiException::class.java)
                    // hacer algo con el token aquí
                    if (token != null) {
                        val credential = GoogleAuthProvider.getCredential(token.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (buttonRemember.isChecked) {
                                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
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
                                val text = "Se ha producido un error al obtener el usuario!"
                                showToast(text)
                            }
                        }
                    }

                }catch (e: ApiException){
                    val text = "Se ha producido un error al obtener el usuario!"
                    showToast(text)

                }
            }
        }


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

            if (editTextEmail.text.isNotEmpty() && editTextPassword.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){

                        if(buttonRemember.isChecked){
                            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                            prefs.putString("email", editTextEmail.text.toString())
                            prefs.putString("password", editTextPassword.text.toString())
                            prefs.apply()

                        }
                        val intent = Intent(this, MainActivity::class.java)
                        val text = "Bienvenido!"
                        showToast(text)

                        // Iniciar la actividad ActivitySignIn
                        startActivity(intent)



             }  else{
                        val text = " La contraseña y/o el email no son correctos"
                        showToast(text)

                    }

                      }
                    } else{
                val text = "Introduzca email y/o contraseña"
                showToast(text)

            }
                }
        }
    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val password = prefs.getString("password", null)

        if (email != null && password != null) {
            val intent = Intent(this, MainActivity::class.java)
            val text = "Bienvenido!"
            showToast(text)

            // Iniciar la actividad ActivitySignIn
            startActivity(intent)
        }


    }

    }




