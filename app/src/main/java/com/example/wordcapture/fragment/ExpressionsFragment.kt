package com.example.wordcapture.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordcapture.R
import com.example.wordcapture.activity.DetailActivity
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.domain.CaptionedImagesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpressionsFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val itemRecycler: RecyclerView =  inflater.inflate(R.layout.fragement_expressions_tab, container, false) as RecyclerView


        CoroutineScope(Dispatchers.IO).launch {
            val expressions = AppDatabase.get(requireContext()).expressionDao().getAll()

            val adapter = CaptionedImagesAdapter(expressions)
            val layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)

            itemRecycler.adapter = adapter
            itemRecycler.layoutManager = layoutManager

            adapter.setListener(object : CaptionedImagesAdapter.CaptionedImageListener {
                override fun onClick(id: Int) {
                    val intent = Intent(activity, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXPRESSION_ID, id)
                    requireActivity().startActivity(intent)
                }
            })

        }

        return itemRecycler
    }

}