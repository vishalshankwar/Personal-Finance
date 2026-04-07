package com.example.personalfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.model.CategorySummary
import com.example.personalfinance.util.Formatters

class CategorySummaryAdapter : RecyclerView.Adapter<CategorySummaryAdapter.CategoryViewHolder>() {

    private val items = mutableListOf<CategorySummary>()

    fun submitList(data: List<CategorySummary>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_summary, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryName)
        private val amountText: TextView = itemView.findViewById(R.id.categoryAmount)
        private val percentageText: TextView = itemView.findViewById(R.id.categoryPercentage)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.categoryProgress)

        fun bind(item: CategorySummary) {
            categoryText.text = item.category
            amountText.text = Formatters.currency(item.amount)
            percentageText.text = "${item.percentage}%"
            progressBar.progress = item.percentage
        }
    }
}
