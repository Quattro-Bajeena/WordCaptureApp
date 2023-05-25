package com.example.wordcapture.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.fragment.ViewExpressionFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_new_expression.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewExpressionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expression)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val addButton = findViewById<Button>(R.id.add_expression_button)
        addButton.setOnClickListener { view ->  addNewExpression() }
    }


    private fun addNewExpression(){


        val fragment =  supportFragmentManager.findFragmentById(R.id.new_expression_fragment) as ViewExpressionFragment
        val newExpression = fragment.getCurrentExpression()

        if(newExpression == null){
            Snackbar
                .make(new_expression_view, "Can't add new expression", Snackbar.LENGTH_LONG)
                .show()
            return
        }

        if(newExpression.language != null){
            val sharedPref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(getString(com.example.wordcapture.R.string.default_language_key), newExpression.language)
                apply()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.get(applicationContext).expressionDao().insert(newExpression)
            val data = Intent()
            data.putExtra(ViewExpressionFragment.EXPRESSION_INSERTED, true)
            setResult(RESULT_OK, data)
            finish()
        }

    }




}