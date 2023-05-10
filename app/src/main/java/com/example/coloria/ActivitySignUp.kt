package com.example.coloria

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.coloria.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class ActivitySignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

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
        binding.buttonSingUp.setOnClickListener {

            if(editTextUserName.text.isNotEmpty()){

                if(editTextPassword.text.toString() == editTextPassConf.text.toString()){
                    if (editTextEmail.text.isNotEmpty() && editTextPassword.text.isNotEmpty()) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString()).addOnCompleteListener() {
                        if(it.isSuccessful){
                            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                            prefs.putString("UserName", editTextUserName.text.toString())
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


}