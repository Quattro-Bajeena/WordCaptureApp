package com.example.wordcapture.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.data.Expression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NewExpressionActivity : AppCompatActivity() {

    lateinit var originalView : EditText
    lateinit var translationView : EditText
    lateinit var languageView: Spinner
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expression)


        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        originalView = findViewById(R.id.original_expression)
        translationView = findViewById(R.id.translated_expression)
        languageView = findViewById(R.id.languages_spinner)
        imageView = findViewById(R.id.imageView)



        val addButton = findViewById<Button>(R.id.add_expression_button)
        val translateButton = findViewById<ImageButton>(R.id.translate_button)


        addButton.setOnClickListener { view ->  addNewExpression() }
        translateButton.setOnClickListener {view -> useTranslator()}
        imageView.setOnClickListener { view -> takePicture() }

        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        val language = sharedPref.getString(getString(R.string.default_language_key), null)
        if(language != null){
            languageView.setSelection( (languageView.adapter as ArrayAdapter<String>).getPosition(language) )
        }
        languageView.prompt

    }

    private fun addNewExpression(){
        val original = originalView.text.toString()
        val translation = translationView.text.toString()
        val language = languageView.selectedItem as String?
        val date = Date()
        val photoPath = currentPhotoPath

        val newExpression = Expression(
            0,
            original,
            translation,
            date,
            language,
            photoPath
        )

        if(language != null){
            val sharedPref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(getString(com.example.wordcapture.R.string.default_language_key), language)
                apply()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.get(applicationContext).expressionDao().insert(newExpression)
            val data = Intent()
            data.putExtra(NEW_EXPRESSION_INSERTED, true)
            setResult(RESULT_OK, data)
            finish()
        }

    }

    companion object{
        const val NEW_EXPRESSION_INSERTED = "new_expression_inserted"
    }


    private fun takePicture(){
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        resultLauncher.launch(cameraIntent)
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
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
                        this,
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
            imageView.setImageURI(Uri.parse(photoPath))
            imageView.setPadding(0)
        }
    }

    private var currentPhotoPath: String? = null
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }





    private fun useTranslator(){
        val baseLanguage = languageView.selectedItem as String?
        if(baseLanguage == null){
            Toast.makeText(this, "No language picked", Toast.LENGTH_LONG).show()

        }
    }


}