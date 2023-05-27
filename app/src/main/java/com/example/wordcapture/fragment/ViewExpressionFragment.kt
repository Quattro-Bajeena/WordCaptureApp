package com.example.wordcapture.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.data.Expression
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.android.synthetic.main.fragment_my_item.*
import kotlinx.android.synthetic.main.fragment_view_expression.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ViewExpressionFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var editable: Boolean = true


    private lateinit var parentActivity: FragmentActivity

    private var expression: Expression? = null
    private lateinit var translator: Translate

    private var expressionId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentActivity = requireActivity()

        arguments?.let {
            val id = it.getInt(ARG_ID)
            expressionId = id
            CoroutineScope(Dispatchers.IO).launch {
                expression = AppDatabase.get(parentActivity).expressionDao().get(id)
            }
            editable = it.getBoolean(ARG_EDITABLE, true)
        }

        translator = getTranslateService()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_expression, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        translate_button.setOnClickListener {view -> useTranslator()}
        imageView.setOnClickListener { view -> takePicture() }

        val sharedPref = parentActivity.getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        val language = sharedPref.getString(getString(R.string.default_language_key), null)
        if(language != null){
            languages_spinner.setSelection( (languages_spinner.adapter as ArrayAdapter<String>).getPosition(language) )
        }

        expression?.let { expr ->
            original_expression.setText(expr.original)
            languages_spinner.setSelection( (languages_spinner.adapter as ArrayAdapter<String>).getPosition(expr.language) )

            expr.translation?.let { translated_expression.setText(it) }
            expr.thumbnailPath?.let { imageView.setImageURI(Uri.parse(it)) }
        }

        toggleEditing(editable)
    }




    public fun getCurrentExpression():Expression?{
        val original = original_expression.text.toString()
        val translation = translated_expression.text.toString()
        val language = languages_spinner.selectedItem as String
        val date = Date()
        val photoPath = currentPhotoPath ?: expression?.imagePath
        val thumbnailPath = currentThumbnailPath ?: expression?.thumbnailPath

        if(original.isBlank() && (photoPath == null || thumbnailPath == null)){
            return null
        }

        val newExpression = Expression(
            expressionId,
            original,
            translation,
            date,
            language,
            photoPath,
            thumbnailPath
        )

        return newExpression
    }

    private fun takePicture(){
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(parentActivity.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        parentActivity,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK ) {
            val photoPath = currentPhotoPath
            val photoThumbnail= ThumbnailUtils.createImageThumbnail(photoPath!!,  MediaStore.Images.Thumbnails.MINI_KIND)
            imageView.setImageBitmap(photoThumbnail)
            imageView.setPadding(0)

            val storageDir = parentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val thumbnailFile = File.createTempFile("THUMBNAIL_${timeStamp}_", ".jpg", storageDir)
            val fOut = FileOutputStream(thumbnailFile)
            photoThumbnail?.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
            fOut.flush()
            fOut.close()

            currentThumbnailPath = thumbnailFile.absolutePath

        }
    }

    private var currentPhotoPath: String? = null
    private var currentThumbnailPath: String? = null
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = parentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun toggleEditing(enabled: Boolean){
        original_expression.isEnabled = enabled
        translated_expression.isEnabled = enabled
        languages_spinner.isEnabled = enabled
        imageView.isEnabled = enabled
        translate_button.isEnabled = enabled
        editable = enabled
    }




    private fun useTranslator(){


        val baseLanguage = languages_spinner.selectedItem as String?
        if(baseLanguage == null){
            Toast.makeText(parentActivity, "No language picked", Toast.LENGTH_LONG).show()
            return
        }

        if (checkInternetConnection() == false) {
            Toast.makeText(parentActivity, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }


        val originalText = original_expression.text.toString()
        val translation: Translation = translator.translate(
            originalText,
            Translate.TranslateOption.sourceLanguage(mapWithValues[baseLanguage]),
            Translate.TranslateOption.targetLanguage("en"),
            Translate.TranslateOption.model("base")
        )
        val translatedText = translation.translatedText

        translated_expression.setText(translatedText)
        Toast.makeText(parentActivity, "Used google translate", Toast.LENGTH_SHORT).show()
    }

    private fun getTranslateService() : Translate{
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        resources.openRawResource(R.raw.translate_credentials).use { `is` ->

            //Get credentials:
            val myCredentials = GoogleCredentials.fromStream(`is`)

            //Set credentials and get translate service:
            val translateOptions =
                TranslateOptions.newBuilder().setCredentials(myCredentials).build()
            return translateOptions.service
        }

    }


    private fun checkInternetConnection(): Boolean {

        //Check internet connection:
        val connectivityManager = parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        //Means that we are connected to a network (mobile or wi-fi)
        val connected = connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        return connected
    }





    companion object {

        val mapWithValues = mapOf(
            "japanese" to "ja",
            "polish" to "pl",
            "german" to "de",
            "spanish" to "es",
            "french" to "fr",
            "russian" to "ru",
            "portuguese" to "pt-PT",
            "mandarin" to "zh-CN"
        )

        private const val ARG_ID = "param0"
        private const val ARG_ORIGINAL = "param1"
        private const val ARG_TRANSLATION = "param2"
        private const val ARG_LANGUAGE = "param3"
        private const val ARG_IMAGE_PATH = "param4"
        private const val ARG_THUMBNAIL_PATH = "param5"
        private const val ARG_EDITABLE = "param6"

        const val EXPRESSION_UPDATED = "expression_changed"
        const val EXPRESSION_INSERTED = "expression_inserted"
        const val EXPRESSION_UPDATED_POS = "expression_changed_pos"

        fun newInstance(id: Int, editable: Boolean) =
            ViewExpressionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                    putBoolean(ARG_EDITABLE, editable)
                }
            }
    }
}