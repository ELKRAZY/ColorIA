package com.example.coloria

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.coloria.CameraActivity.Companion.TAG
import com.example.coloria.colorPicker.ColorDetectHandler
import com.example.coloria.databinding.ActivityFullPhotoBinding
import com.example.coloria.viewModel.SavedPhotosViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File

class FullPhotoActivity : AppCompatActivity() {

    private val detectHandler = ColorDetectHandler()

    private lateinit var binding: ActivityFullPhotoBinding
            private var index: Int = 0
    private lateinit var uri: Uri

    private lateinit var fileName: String
    private val db = Firebase.firestore
    private val users = FirebaseAuth.getInstance().currentUser
    private val email = users?.email



    // Views
    private lateinit var photo: ImageView
    private lateinit var deleteBtn: Button
    private lateinit var fpCardColorPreview: CardView
    private lateinit var fpCardColor: CardView
    private lateinit var fpColorHex: TextView
    private lateinit var fpPointer: View
    private lateinit var fpColorName: TextView
    private lateinit var fpCardColorName: TextView

    private val savedPhotosViewModel = SavedPhotosViewModel()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInit()

        fpColorHex.setOnClickListener {

            val text = fpColorHex.text.toString()
            copyText(text)
            val usersCollectionRef = db.collection("colorlist").document(email.toString())

            usersCollectionRef.get().addOnSuccessListener { documentSnapshot ->
                val colorArrayList = documentSnapshot.get("colorArrayList") as? ArrayList<String>
                colorArrayList?.add(text) // Añade el color al ArrayList existente o crea uno nuevo si es nulo

                usersCollectionRef.update("colorArrayList", colorArrayList)
                    .addOnSuccessListener {
                        // La actualización se realizó con éxito
                        Log.d(TAG, "Color added to ArrayList in Firebase")
                    }
                    .addOnFailureListener { e ->
                        // Ocurrió un error al realizar la actualización
                        Log.e(TAG, "Failed to add color to ArrayList in Firebase", e)
                    }
            }.addOnFailureListener { e ->
                // Ocurrió un error al obtener el documento de Firebase
                Log.e(TAG, "Failed to get document from Firebase", e)
            }
        }

        fpColorName.setOnClickListener {
            copyText(fpColorName.text.toString())
        }

        deleteBtn.setOnClickListener {

            fileName = getPhotoList()[index].name

            showDialog()

        }

        photo.setOnTouchListener { _, motionEvent ->

            setCardinates(motionEvent)

            detect(motionEvent)

            true
        }
    }

    private fun setCardinates(motionEvent: MotionEvent){

        fpCardColorPreview.y = motionEvent.y
        fpCardColorPreview.x = motionEvent.x

        fpPointer.y = fpCardColorPreview.y
        fpPointer.x = fpCardColorPreview.x

        fpCardColorPreview.y = motionEvent.y - 100
        fpCardColorPreview.x = motionEvent.x - motionEvent.x / 2

        if (fpPointer.x >= photo.right - 50f){
            fpPointer.x = photo.right - 50f
        }
    }

    private fun setInit() {

        // Views
        photo = binding.specificPhotoImageView
        deleteBtn = binding.deleteBtn
        fpPointer = binding.fpPointer
        fpCardColor = binding.fpCardColor
        fpCardColorPreview = binding.fpCardColorPreview
        fpColorHex = binding.fpColorHex
        fpColorName = binding.fpColorName
        fpCardColorName = binding.fpCardColorName

        // Value
        index = intent.getIntExtra("photoIndex", 0)
        val uriString = intent.getStringExtra("uri")


        if (uriString != null){
            uri = Uri.parse(uriString)
            Glide.with(this).load(uri).into(photo)
            deleteBtn.visibility = View.INVISIBLE

        }else{
            Glide.with(this).load(getPhotoList()[index]).into(photo)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun detect(motionEvent: MotionEvent) {

        if (motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_DOWN) {

            val currColor = detectHandler.detect(photo, fpPointer)

            val name = currColor.name
            val hex = currColor.hex
            val r = currColor.r
            val g = currColor.g
            val b = currColor.b

            fpColorHex.text = "#$hex"
            fpColorName.text = name
            fpCardColorName.text = name

            fpCardColor.setCardBackgroundColor(Color.rgb(r!!, g!!, b!!))

        }
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.apply {
            setIcon(R.drawable.ic_delete)
            setTitle("Delete")
            setMessage("Are you sure to delete this photo ?")
            setPositiveButton("Yes") { _, _ ->
                delete(fileName)
                intentToSavedPhotos()
                Toast.makeText(context,"Successfully deleted",Toast.LENGTH_LONG).show()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

            }

        }.create().show()
    }

    private fun copyText(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copy_text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(applicationContext, "Copied $text", Toast.LENGTH_SHORT).show()


    }



    private fun intentToSavedPhotos(){
        val intent = Intent(this, SavedPhotoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun delete(fileName: String) {

        val file = File(externalMediaDirs[0], fileName)

        if (file.exists()) {
            file.delete()
        }
    }

    private fun getPhotoList(): Array<File> {

        return savedPhotosViewModel.getPhotos(this)

    }
}
