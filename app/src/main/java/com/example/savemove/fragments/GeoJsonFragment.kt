package com.example.savemove.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.savemove.R
import com.example.savemove.adapters.GeoJsonAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_geo_json_list.*

class GeoJsonFragment : Fragment() {
    private var listaGeoJson: MutableList<GeoJsonItem> = mutableListOf<GeoJsonItem>()
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_geo_json_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Firebase.firestore.collection("Mapa de Calor")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(mContext, "No hay datos disponibles", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val item = GeoJsonItem(
                            document.data["title"].toString(),
                            document.data["description"].toString(),
                            document.data["file"].toString()
                        )
                        listaGeoJson.add(item)
                    }
                    val adapter = GeoJsonAdapter(
                        listaGeoJson
                    )
                    if (geojsonList != null) {
                        geojsonList.adapter = adapter
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(mContext, exception.toString(), Toast.LENGTH_LONG).show()
            }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }
}