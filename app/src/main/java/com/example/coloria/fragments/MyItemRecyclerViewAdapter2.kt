package com.example.coloria.fragments

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coloria.databinding.ListFavItemColorBinding


class MyItemRecyclerViewAdapter2(private val colors: List<String>) : RecyclerView.Adapter<MyItemRecyclerViewAdapter2.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListFavItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color)
        
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    inner class ViewHolder(private val binding: ListFavItemColorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: String) {
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