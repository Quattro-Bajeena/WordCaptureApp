package com.example.wordcapture.activity



import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.fragment.ExpressionDetailFragment
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

        CoroutineScope(Dispatchers.IO).launch {
            val expression = AppDatabase.get(applicationContext).expressionDao().get(id)
            supportActionBar?.title = expression.original
        }

        if(savedInstanceState == null){
            val detail = ExpressionDetailFragment.newInstance(id)
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.add(R.id.detail_frag, detail)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.commit()
        }


    }

    companion object{
        const val EXPRESSION_ID  = "id"
        const val EXPRESSION_ORIGINAL = "original"
    }
}