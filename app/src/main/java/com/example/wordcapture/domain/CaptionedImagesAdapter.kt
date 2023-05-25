package com.example.wordcapture.domain

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.wordcapture.R
import com.example.wordcapture.data.Expression

class CaptionedImagesAdapter(var expressions: List<Expression>) :
    RecyclerView.Adapter<CaptionedImagesAdapter.ViewHolder>() {

    private var listener : CaptionedImageListener? = null

    class ViewHolder(v: CardView) : RecyclerView.ViewHolder(v) {
        var cardView: CardView = v

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cv = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_captioned_image, parent, false) as CardView
        return ViewHolder(cv)
    }

    override fun getItemCount(): Int {
        return expressions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView
        val expression = expressions[position]

        if(expression.thumbnailPath != null){
            val imageView = cardView.findViewById<ImageView>(R.id.expression_image)
            imageView.setImageURI(Uri.parse(expression.thumbnailPath))
            imageView.contentDescription = expression.original
        }

        if(expression.translation != null){
            val translationView = cardView.findViewById<TextView>(R.id.expression_translation)
            translationView.text = expression.translation
        }

        val textView = cardView.findViewById<TextView>(R.id.expression_original)
        textView.text = expression.original


        cardView.setOnClickListener { listener?.onClick(expression.id) }
    }

    interface CaptionedImageListener{
        fun onClick(id: Int)
    }

    fun setListener(listener: CaptionedImageListener){
        this.listener = listener
    }




}