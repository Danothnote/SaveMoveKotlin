package com.example.savemove.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.savemove.MainActivity
import com.example.savemove.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_upload_tourism.*
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val LATITUDE = "lat"
private const val LONGITUDE = "lon"

/**
 * A simple [Fragment] subclass.
 * Use the [UploadTourismFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadTourismFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var lat: Double? = null
    private var lon: Double? = null

    private lateinit var mContext: Context
    private lateinit var tImg: ImageView
    private lateinit var tBtn: Button
    private lateinit var editTTitle: EditText
    private lateinit var editTProvincia: EditText
    private lateinit var editTDescription: EditText
    private lateinit var tLat: String
    private lateinit var tLog: String
    private lateinit var filePath: Uri
    private lateinit var mStorageRef: StorageReference
    private lateinit var nameImg: String
    private var fileSelected = false
    private var tsLong:Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lat = it.getDouble(LATITUDE)
            lon = it.getDouble(LONGITUDE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_upload_tourism, container, false)
        tImg = view.findViewById(R.id.tImg)
        editTTitle = view.findViewById(R.id.editTTitle)
        editTProvincia = view.findViewById(R.id.editTProvincia)
        editTDescription = view.findViewById(R.id.editTDescription)
        tBtn = view.findViewById(R.id.tBtn)

        tImg.setOnClickListener{
            showFileChooser()
        }
        tBtn.setOnClickListener{
            if (fileSelected && editTTitle.text.toString() != "" && editTProvincia.text.toString() != "" && editTDescription.text.toString() != "") {
                uploadFile()
                uploadDoc()
                (mContext as MainActivity).closeFragment()
                (mContext as MainActivity).finishUploadTourism()
            } else {
                Toast.makeText(mContext, "Los datos no están completos", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun showFileChooser() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            Glide.with(context!!).load(filePath).into(tImg)
            fileSelected = true
        }
    }

    private fun uploadDoc() {
        val document = hashMapOf(
            "title" to editTTitle.text.toString(),
            "provincia" to editTProvincia.text.toString(),
            "description" to editTDescription.text.toString(),
            "img" to nameImg,
            "latitude" to lat,
            "longitude" to lon
        )
        Firebase.firestore.collection("Lugares Recomendados")
            .document()
            .set(document)
            .addOnSuccessListener { Toast.makeText(mContext, "Documento creado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e: Exception -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show() }
    }

    private fun uploadFile() {
        tsLong = System.currentTimeMillis()/1000
        mStorageRef = FirebaseStorage.getInstance().reference
        val file = filePath
        nameImg = "$tsLong${file.lastPathSegment}"
        val geojsonRef = mStorageRef.child("Recomendados/$nameImg")
        val uploadTask = geojsonRef.putFile(file)
        uploadTask.addOnFailureListener{e: Exception ->
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {
            Toast.makeText(mContext, "Datos subidos correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UploadTourismFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(lat: Double, lon: Double) =
            UploadTourismFragment().apply {
                arguments = Bundle().apply {
                    putDouble(LATITUDE, lat)
                    putDouble(LONGITUDE, lon)
                }
            }
    }
}