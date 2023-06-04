package com.example.coloria.viewModel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class SavedPhotosViewModel : ViewModel() {

    var spError = MutableLiveData<Boolean>()
    var spNotFound = MutableLiveData<Boolean>()
    private lateinit var photos: Array<File>
    var spList: MutableLiveData<Array<File>> = MutableLiveData()

    fun getPhotos(context: Context): Array<File> {

        val directory = File(context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES)[0].absolutePath)
        photos = directory.listFiles() as Array<File>
        spList.value = photos

        if (photos.isEmpty()) {
            spNotFound.value = true
            spError.value = false
        } else {
            spError.value = false
            spNotFound.value = false
        }

        return photos
    }

}