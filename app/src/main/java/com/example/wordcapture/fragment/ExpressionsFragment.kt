package com.example.wordcapture.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var listener: ExpressionUpdatedListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val itemRecycler: RecyclerView =  inflater.inflate(R.layout.fragement_expressions_tab, container, false) as RecyclerView


        CoroutineScope(Dispatchers.IO).launch {
            val expressions = AppDatabase.get(requireContext()).expressionDao().getAll()

            val adapter = CaptionedImagesAdapter(expressions)
            val layoutManager = LinearLayoutManager(activity,  GridLayoutManager.VERTICAL, false)
//            val layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
//            val layoutManager = StaggeredGridLayoutManager(4, LinearLayoutManager.VERTICAL)

            itemRecycler.adapter = adapter
            itemRecycler.layoutManager = layoutManager

            adapter.setListener(object : CaptionedImagesAdapter.CaptionedImageListener {
                override fun onClick(id: Int) {
                    val intent = Intent(activity, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXPRESSION_ID, id)
                    intent.putExtra(DetailActivity.EXPRESSION_POS, id)
//                    requireActivity().startActivity(intent)
                    resultLauncherViewExpression.launch(intent)
                }
            })

        }

        return itemRecycler
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ExpressionUpdatedListener
    }

    val resultLauncherViewExpression = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> listener.expressionUpdated(result) }

    interface ExpressionUpdatedListener{
        fun expressionUpdated(result: ActivityResult)
    }

}