package com.example.savemove.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.savemove.MainActivity
import com.example.savemove.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mapbox.mapboxsdk.geometry.LatLng

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TTITLE = "ttitle"
private const val TDESCRIPTION = "tdescription"
private const val TIMG = "timg"
private const val TLATITUDE = "tlatitude"
private const val TLONGITUDE = "tlongitude"

/**
 * A simple [Fragment] subclass.
 * Use the [ItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var ttitle: String? = null
    private var tdescription: String? = null
    private var timg: String? = null
    private var tlatitude: Double? = null
    private var tlongitude: Double? = null

    private lateinit var mContext: Context

    private lateinit var editTitle: TextView
    private lateinit var editDescription: TextView
    private lateinit var buttonFragment: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ttitle = it.getString(TTITLE)
            tdescription = it.getString(TDESCRIPTION)
            timg = it.getString(TIMG)
            tlatitude = it.getDouble(TLATITUDE)
            tlongitude = it.getDouble(TLONGITUDE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_item, container, false)
        buttonFragment = view.findViewById(R.id.tBtn)
        buttonFragment.setOnClickListener{
            val point = LatLng(tlatitude!!, tlongitude!!)
            (mContext as MainActivity).goToPlace(point, tlatitude!!, tlongitude!!)
            (mContext as MainActivity).closeFragment()
        }
        editTitle = view.findViewById(R.id.tTitle)
        editTitle.text = ttitle
        editDescription = view.findViewById(R.id.tDescription)
        editDescription.text = tdescription
        Firebase.storage.reference.child("//Recomendados/${timg}").downloadUrl.addOnSuccessListener {
                uri ->
            val img:ImageView = view.findViewById<ImageView>(R.id.tImg)
            Glide.with(context!!).load(uri).into(img)
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ItemFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String, param4: Double, param5: Double) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putString(TTITLE, param1)
                    putString(TDESCRIPTION, param2)
                    putString(TIMG, param3)
                    putDouble(TLATITUDE, param4)
                    putDouble(TLONGITUDE, param5)
                }
            }
    }
}