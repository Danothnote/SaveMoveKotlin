package com.example.savemove.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.savemove.MainActivity
import com.example.savemove.R
import com.example.savemove.fragments.GeoJsonItem

class GeoJsonAdapter(private val listaGeoJson: List<GeoJsonItem>) : RecyclerView.Adapter<GeoJsonAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_geo_json, parent, false)
        return ViewHolder(view).listen { pos, type ->
            val item = listaGeoJson[pos]
            (parent.context as MainActivity).fileHeatMap(item.file)
            (parent.context as MainActivity).closeFragment()
        }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaGeoJson[position]
        holder.titleView.text = item.title
        holder.descriptionView.text = item.description
    }

    override fun getItemCount(): Int = listaGeoJson.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.title)
        val descriptionView: TextView = view.findViewById(R.id.description)
    }
}