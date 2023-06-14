package com.example.coloria.fragments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coloria.databinding.ListItemColorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyItemRecyclerViewAdapter(private val colors: List<String>) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private val users = FirebaseAuth.getInstance().currentUser
    private val email = users?.email
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color)

        holder.favoriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val usersCollectionRef = db.collection("colorlist").document(email.toString())


            usersCollectionRef.get().addOnSuccessListener { documentSnapshot ->
                val favColorList = documentSnapshot.get("favColorList") as ArrayList<String>
                if (isChecked) {
                    if(!favColorList.contains(color)){
                        favColorList.add(color) // Añade el color al ArrayList existente o crea uno nuevo si es nulo

                    }

                } else {
                    favColorList.remove(color)
                }

                usersCollectionRef.update("favColorList", favColorList)
                    .addOnSuccessListener {
                        // La actualización se realizó con éxito
                        //Log.d(CameraActivity.TAG, "Color added to ArrayList in Firebase")
                    }
                    .addOnFailureListener {
                        // Ocurrió un error al realizar la actualización
                        //Log.e(CameraActivity.TAG, "Failed to add color to ArrayList in Firebase", e)
                    }
            }.addOnFailureListener {
                // Ocurrió un error al obtener el documento de Firebase
                //Log.e(CameraActivity.TAG, "Failed to get document from Firebase", e)
            }


        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    inner class ViewHolder(private val binding: ListItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        val favoriteCheckbox =  binding.favoriteCheckbox
        private val usersCollectionRef = db.collection("colorlist").document(email.toString())


        fun bind(color: String) {
            usersCollectionRef.get().addOnSuccessListener { documentSnapshot ->
                val favColorList = documentSnapshot.get("favColorList") as? ArrayList<*>

                if (favColorList != null) {
                    favoriteCheckbox.isChecked = favColorList.contains(color)
                }
            }.addOnFailureListener {
                // Ocurrió un error al obtener el documento de Firebase
                //Log.e(CameraActivity.TAG, "Failed to get document from Firebase", e)
            }

            binding.colorSquare.setBackgroundColor(Color.parseColor(color))
            binding.hexValue.text = color
            binding.rgbValue.text = convertHexToRgb(color)

        }

        private fun convertHexToRgb(hex: String): String {
            val color = Color.parseColor(hex)
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return "RGB: $red, $green, $blue"
        }
    }
}
