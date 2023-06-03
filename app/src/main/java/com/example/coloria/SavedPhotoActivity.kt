package com.example.coloria

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coloria.colorPicker.SavedPhotosAdapter
import com.example.coloria.viewModel.SavedPhotosViewModel
import java.io.File

class SavedPhotoActivity : AppCompatActivity() {


    private lateinit var savedPhotosRv: RecyclerView
    private lateinit var savedPhotosViewModel: SavedPhotosViewModel
    private lateinit var files: Array<File>

    private lateinit var notFoundText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_photo)

        savedPhotosViewModel = ViewModelProvider(this)[SavedPhotosViewModel::class.java]

        savedPhotosRv = findViewById(R.id.saved_photos_rv)
        savedPhotosRv.layoutManager = GridLayoutManager(this, 3)

        notFoundText = findViewById(R.id.noPhotoTextView)

        files = savedPhotosViewModel.getphotos(this)

        val adapter = SavedPhotosAdapter(files.reversedArray(), this)

        savedPhotosRv.adapter = adapter

        savedPhotosViewModel.getphotos(this)
        oberserData()

    }

    private fun oberserData() {

        savedPhotosViewModel.spList.observe(this) {

            if (it.isNotEmpty()) {
                notFoundText.visibility = View.GONE
                savedPhotosRv.visibility = View.VISIBLE
            }
        }

        savedPhotosViewModel.spError.observe(this) {

            if (it) {
                notFoundText.visibility = View.GONE
                savedPhotosRv.visibility = View.GONE
            }
        }

        savedPhotosViewModel.spNotFound.observe(this) {

            if (it) {
                notFoundText.visibility = View.VISIBLE
                savedPhotosRv.visibility = View.GONE
            }
        }
    }
}
