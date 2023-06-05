package com.example.coloria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coloria.databinding.FragmentHistoryListBinding

class FragmentHistory : Fragment() {

    private var columnCount = 1
    private lateinit var binding: FragmentHistoryListBinding
    private lateinit var adapter: MyItemRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryListBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set up RecyclerView
        val recyclerView = binding.colorHistoryView
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        adapter = MyItemRecyclerViewAdapter(getColorList())
        recyclerView.adapter = adapter

        return view
    }

    private fun getColorList(): List<String> {
        // Aquí puedes proporcionar la lista de colores que deseas mostrar en el RecyclerView
        return listOf("#FF0000", "#00FF00", "#0000FF")
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
