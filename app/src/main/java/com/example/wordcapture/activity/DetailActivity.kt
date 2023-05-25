package com.example.wordcapture.activity



import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.fragment.ViewExpressionFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DetailActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val id = intent.extras!!.getInt(EXPRESSION_ID)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(savedInstanceState == null){
            val detail = ViewExpressionFragment.newInstance(id, false)
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.add(R.id.expression_fragment, detail)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.commit()
        }

        edit_toggle.setOnCheckedChangeListener { _, b -> toggleEdit(b) }
        update_expression_button.setOnClickListener { updateExpression() }

    }

    private fun toggleEdit(enabled: Boolean){
        val fragment =  supportFragmentManager.findFragmentById(R.id.expression_fragment) as ViewExpressionFragment
        update_expression_button.isEnabled = enabled
        fragment.toggleEditing(enabled)
    }

    private fun updateExpression(){
        val fragment =  supportFragmentManager.findFragmentById(R.id.expression_fragment) as ViewExpressionFragment
        val newExpression = fragment.getCurrentExpression()
        if(newExpression != null){
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.get(applicationContext).expressionDao().update(newExpression)
                val data = Intent()
                data.putExtra(ViewExpressionFragment.EXPRESSION_UPDATED, true)
                setResult(RESULT_OK, data)
                finish()

            }
        }
        else{
            Snackbar
                .make(expression_detail_view, "Can't update expression", Snackbar.LENGTH_LONG)
                .show()
        }

    }


    companion object{
        const val EXPRESSION_ID  = "id"
        const val EXPRESSION_ORIGINAL = "original"
        const val EXPRESSION_POS = "pos"
    }

}