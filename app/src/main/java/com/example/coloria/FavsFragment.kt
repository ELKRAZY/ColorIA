package com.example.coloria

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coloria.databinding.FragmentHistoryListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.CheckBox
import com.example.coloria.databinding.FragmentFavsBinding
import com.example.coloria.databinding.FragmentFavsListBinding


class FavsFragment : Fragment() {

    private var columnCount = 1
    private lateinit var binding: FragmentFavsListBinding
    private lateinit var adapter: MyItemRecyclerViewAdapter2
    private val db = Firebase.firestore
    private val users = FirebaseAuth.getInstance().currentUser
    private val email = users?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavsListBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set up RecyclerView
        val recyclerView = binding.FavColorView
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        val usersCollectionRef = db.collection("colorlist").document(email.toString())
        var colorList: List<String>


        usersCollectionRef.get().addOnSuccessListener { documentSnapshot ->
            val favColorList = documentSnapshot.get("favColorList") as? ArrayList<String>
            if (favColorList != null) {
                colorList = favColorList
                adapter = MyItemRecyclerViewAdapter2(colorList)
                recyclerView.adapter = adapter

                binding.sendButton.setOnClickListener {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, colorList.joinToString(", "))
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)

                }
            }

        }.addOnFailureListener { e ->
            // Ocurrió un error al obtener el documento de Firebase
            Log.e(CameraActivity.TAG, "Failed to get document from Firebase", e)
        }



        return view
    }


    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            FragmentHistory().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
