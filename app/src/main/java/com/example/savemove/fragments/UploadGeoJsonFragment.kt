package com.example.savemove.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.savemove.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UploadGeoJsonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadGeoJsonFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mContext: Context
    private lateinit var geojsonFile: ImageView
    private lateinit var gBtn: Button
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var filePath: Uri
    private lateinit var mStorageRef: StorageReference
    private var fileSelected = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_upload_geo_json, container, false)
        geojsonFile = view.findViewById(R.id.gImg)
        editTitle = view.findViewById(R.id.geditTitle)
        editDescription = view.findViewById(R.id.geditDescription)
        gBtn = view.findViewById(R.id.gBtn)

        geojsonFile.setOnClickListener{
            showFileChooser()
        }
        gBtn.setOnClickListener{
            if (fileSelected && editTitle.text.toString() != "" && editDescription.text.toString() != "") {
                uploadDoc()
                uploadFile()
            } else {
                Toast.makeText(mContext, "Los datos no están completos", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun showFileChooser() {
        val intent:Intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleccione un archivo GeoJSON"), 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            fileSelected = true
        }
    }

    private fun uploadDoc() {
        val document = hashMapOf(
            "title" to editTitle.text.toString(),
            "description" to editDescription.text.toString(),
            "file" to filePath.lastPathSegment.toString()
        )
        Firebase.firestore.collection("Mapa de Calor")
            .document()
            .set(document)
            .addOnSuccessListener { Toast.makeText(mContext, "Documento creado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e: Exception -> Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show() }
    }

    private fun uploadFile() {
        mStorageRef = FirebaseStorage.getInstance().reference
        val file = filePath
        val geojsonRef = mStorageRef.child("GeoJSON/${file.lastPathSegment}")
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
         * @return A new instance of fragment UploadGeoJsonFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UploadGeoJsonFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}