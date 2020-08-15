package com.example.savemove.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.savemove.MainActivity
import com.example.savemove.R
import com.example.savemove.fragments.TourismItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import java.net.URI

class TourismAdapter(private val listaTurismo: List<TourismItem>) : RecyclerView.Adapter<TourismAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_tourism, parent, false)
        return ViewHolder(view).listen { pos, type ->
            val item = listaTurismo[pos]
            val title: String = item.title
            val description: String = item.description
            val img: String = item.img
            val latitude: Double = item.latitude
            val longitude: Double = item.longitude
            (parent.context as MainActivity).showItem(title, description, img, latitude, longitude)
        }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaTurismo[position]
        holder.titleView.text = item.title
        holder.provinciaView.text = item.provincia
        Firebase.storage.reference.child("//Recomendados/${item.img}").downloadUrl.addOnSuccessListener {
                uri ->
            Glide.with(context).load(uri).into(holder.imgView)
        }
    }

    override fun getItemCount(): Int = listaTurismo.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.tlTitle)
        val provinciaView: TextView = view.findViewById(R.id.tlProvincia)
        val imgView: ImageView = view.findViewById(R.id.tlImg)
    }
}