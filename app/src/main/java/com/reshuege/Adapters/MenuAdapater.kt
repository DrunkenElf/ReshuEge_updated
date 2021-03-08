package com.reshuege.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.reshuege.R


class MenuRecAdapter(
    val context: Context, val mas: Array<String>,
    val listener: (String) -> Unit,
) :
    RecyclerView.Adapter<MenuRecAdapter.MenuViewHolder>() {

    private val imgs = intArrayOf(
        R.drawable.ic_tests, R.drawable.ic_training,
        R.drawable.ico_exam, R.drawable.ic_theory, R.drawable.ic_search,
        R.drawable.teacher, R.drawable.ic_stats, R.drawable.ic_manual
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(this.context).inflate(R.layout.startpage_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bindItems()
        holder.itemView.setOnClickListener {
            listener(mas[position])
        }

    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card = itemView.findViewById<CardView>(R.id.start_card_item)
        val img = itemView.findViewById<ImageView>(R.id.start_item_img)
        val title = itemView.findViewById<TextView>(R.id.start_item_text)


        fun bindItems() {
            title.text = mas[adapterPosition]
            img.setImageResource(imgs[adapterPosition])
        }

    }
}
