package com.example.coloria

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


class FragmentHistory : Fragment() {

    private var columnCount = 1
    private lateinit var binding: FragmentHistoryListBinding
    private lateinit var adapter: MyItemRecyclerViewAdapter
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
        binding = FragmentHistoryListBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set up RecyclerView
        val recyclerView = binding.colorHistoryView
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        val usersCollectionRef = db.collection("colorlist").document(email.toString())
        var colorList: List<String>


        usersCollectionRef.get().addOnSuccessListener { documentSnapshot ->
            val colorArrayList = documentSnapshot.get("colorArrayList") as? ArrayList<String>
            if (colorArrayList != null) {
                colorList = colorArrayList
                adapter = MyItemRecyclerViewAdapter(colorList)
                recyclerView.adapter = adapter
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
