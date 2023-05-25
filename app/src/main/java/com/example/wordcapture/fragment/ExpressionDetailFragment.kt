package com.example.wordcapture.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class ExpressionDetailFragment : Fragment() {

    private var expressionId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let { expressionId = it.getInt(EXPRESSION_ID) }
        arguments?.let { expressionId = it.getInt(EXPRESSION_ID) }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_item, container, false)
    }

    override fun onStart() {
        super.onStart()
        val thisView = view
        if (thisView != null) {

            val title = thisView.findViewById<TextView>(R.id.original_text)
            val description = thisView.findViewById<TextView>(R.id.translation_text)
            val imageView = thisView.findViewById<ImageView>(R.id.expression_image)

            CoroutineScope(Dispatchers.IO).launch {
                val expression = AppDatabase.get(requireContext()).expressionDao().get(expressionId)

                title.text = expression.original
                description.text = expression.translation
                imageView.setImageURI(Uri.parse(expression.imageFilename))
            }
        }
    }



    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(EXPRESSION_ID, expressionId)
    }


    companion object {
        const val EXPRESSION_ID = "id"

        fun newInstance(id: Int): ExpressionDetailFragment {
            val fragment = ExpressionDetailFragment()

            val bundle = Bundle().apply {
                putInt(EXPRESSION_ID, id)
            }
            fragment.arguments = bundle

            return fragment
        }
    }

}