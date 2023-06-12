package com.example.coloria

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
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

                    if (validateEmail(editTextEmail) && validatePassword(editTextPassword, editTextPassConf) && validateUserName(editTextUserName)) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString()).addOnCompleteListener {
                        if(it.isSuccessful){
                            val user = hashMapOf("userName" to editTextUserName.text.toString(), "email" to editTextEmail.text.toString(), "pfp" to "")
                            val colorArrayList = ArrayList<String>()
                            db.collection("users").document(editTextEmail.text.toString())
                                .set(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        val prefs = getSharedPreferences(
                                            getString(R.string.prefs_file),
                                            Context.MODE_PRIVATE
                                        ).edit()
                                        prefs.putString("UserName", editTextUserName.text.toString())
                                        prefs.apply()

//                                        db.collection("colorlist").document(editTextEmail.text.toString())
//                                            .set(colorArrayList)


                                        val intent = Intent(this, AuthActivity::class.java)
                                        val text = "Bienvenido!"
                                        showToast(text)

                                        // Iniciar la actividad ActivitySignIn
                                        startActivity(intent)
                                    } else {
                                        val text = "Error al guardar el usuario en la base de datos!"
                                        showToast(text)
                                    }
                                }

                            // Iniciar la actividad ActivitySignIn
                            startActivity(intent)
                        }

                    }

                    }else{
                    val text = "Ha sucedido un error"
                    showToast(text)
            }

        }
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
                                            val prefs = getSharedPreferences(getString(R.string.gprefs_file), Context.MODE_PRIVATE).edit()
                                            prefs.putString("google_id_token", token.idToken)
                                            prefs.apply()

                                            val intent = Intent(this, MainActivity::class.java)
                                            val text = "Bienvenido!"
                                            showToast(text)

                                            // Iniciar la actividad ActivitySignIn
                                            startActivity(intent)
                                        }
                                    } else {
                                        // El documento no existe
                                        val user = hashMapOf("userName" to username, "email" to email, "pfp" to "")
                                        db.collection("users").document(email.toString())
                                            .set(user)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    val prefs = getSharedPreferences(getString(R.string.gprefs_file), Context.MODE_PRIVATE).edit()
                                                    prefs.putString("google_id_token", token.idToken)
                                                    prefs.apply()

                                                val colorArrayList = ArrayList<String>()

                                                db.collection("colorlist").document(email.toString())
                                                    .set(colorArrayList)

                                                    val intent = Intent(this, MainActivity::class.java)
                                                    val text = "Bienvenido!"
                                                    showToast(text)

                                                    // Iniciar la actividad ActivitySignIn
                                                    startActivity(intent)
                                                } else {
                                                    val text = "Error al guardar el usuario en la base de datos!"
                                                    showToast(text)
                                                }
                                            }
                                    }
                                }.addOnFailureListener { exception ->
                                    // Manejar la excepción, si ocurre
                                }

                            }

                            // Iniciar la actividad ActivitySignIn
                            //startActivity(intent)
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


    private fun validateUserName(editTextUserName: EditText): Boolean {
        val userName = editTextUserName.text.toString().trim()
        return if (userName.isNotEmpty()) {
            true
        } else {
            val text = "No hay nombre de usuario!"
            showToast(text)
            editTextUserName.error = "No hay nombre de usuario"
            false
        }
    }

    private fun validatePassword(editTextPassword: EditText, editTextPassConf: EditText): Boolean {
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextPassConf.text.toString().trim()
        return if (password.isNotEmpty()) {
            if (password.length > 6) {
            if (password == confirmPassword) {
                true
            } else {
                val text = "Las contraseñas no coinciden!"
                showToast(text)
                editTextPassConf.error = "Las contraseñas no coinciden"
                false
            }
        } else {
            val text = "La contraseña tiene que tener minimo 6 caracteres minimo!"
            showToast(text)
            editTextPassword.error = "Introduzca una contraseña válida"
            false
        }
        }else
        {
            val text = "Introduzca una contraseña!"
            showToast(text)
            editTextPassword.error = "Introduzca una contraseña"
            false
        }
    }

    private fun validateEmail(editTextEmail: EditText): Boolean {
        val email = editTextEmail.text.toString().trim()
        return if (email.isNotEmpty()) {
            true
        } else {

            val text = "No hay email o no es válido!"
            showToast(text)
            editTextEmail.error = "Introduzca un email"
            false
        }
    }


    private fun showToast(text: String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

}