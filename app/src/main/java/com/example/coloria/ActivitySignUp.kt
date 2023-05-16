package com.example.coloria

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import com.example.coloria.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ActivitySignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val db = Firebase.firestore
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar)
        supportActionBar?.customView?.findViewById<TextView>(R.id.title_actionbar)?.text = "Crear Cuenta"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24) // Agrega esta línea

        setup()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    private fun setup(){
        val editTextEmail = binding.editTextEmail
        val email = editTextEmail.text.toString()
        val editTextPassword = binding.editTextPassword
        val editTextPassConf = binding.editTextTextPasswordConfirm
        val editTextUserName = binding.editTextTextUserName



        binding.buttonRegGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)

            googleClient.signOut()
            signInLauncher.launch(googleClient.signInIntent)

        }

        binding.buttonSingUp.setOnClickListener {

            if(editTextUserName.text.isNotEmpty()){

                if(editTextPassword.text.toString() == editTextPassConf.text.toString()){
                    if (editTextEmail.text.isNotEmpty() && editTextPassword.text.isNotEmpty()) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString()).addOnCompleteListener {
                        if(it.isSuccessful){
                            db.collection("users").document(email).set(
                                hashMapOf("userName" to editTextUserName.text.toString())
                            )
                            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                            prefs.putString("UserName", editTextUserName.text.toString())
                            prefs.apply()

                            val intent = Intent(this, AuthActivity::class.java)
                            val text = "Bienvenido!"
                            showToast(text)

                            // Iniciar la actividad ActivitySignIn
                            startActivity(intent)
                        }

                    }

                    }else{
                    val text = "Introduzca email y/o contraseña"
                    showToast(text)

            }
            }
                else{
                    editTextPassConf.error = "Las contraseñas no coinciden"
                }


            }else{
                editTextUserName.error = "No hay nombre de usuario"
            }
        }
    }

    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private  val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // El inicio de sesión fue exitoso, puedes obtener el token de acceso aquí
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val token = task.getResult(ApiException::class.java)
                // hacer algo con el token aquí
                if (token != null) {
                    val credential = GoogleAuthProvider.getCredential(token.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {

                            val prefs = getSharedPreferences(getString(R.string.gprefs_file), Context.MODE_PRIVATE).edit()
                            prefs.putString("google_id_token", token.idToken)
                            prefs.apply()

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
        } else{
            val text = "Algo ha fallado!"
            showToast(text)
        }
    }


}