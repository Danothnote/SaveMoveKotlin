package com.example.savemove.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.savemove.R
import com.example.savemove.adapters.TourismAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_tourism_list.*
import java.net.URI

/**
 * A fragment representing a list of Items.
 */
class TourismFragment : Fragment() {

    private var listaTurismo: MutableList<TourismItem> = mutableListOf<TourismItem>()
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
        return inflater.inflate(R.layout.fragment_tourism_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Firebase.firestore.collection("Lugares Recomendados")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(mContext, "No hay datos disponibles", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val item = TourismItem(
                            document.data["title"].toString(),
                            document.data["provincia"].toString(),
                            document.data["description"].toString(),
                            document.data["img"].toString(),
                            document.data["latitude"] as Double,
                            document.data["longitude"] as Double
                        )
                        listaTurismo.add(item)

                    }
                    val adapter = TourismAdapter(
                        listaTurismo
                    )
                    if (tourismList != null) {
                        tourismList.adapter = adapter
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(mContext, exception.toString(), Toast.LENGTH_LONG).show()
            }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TourismFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}