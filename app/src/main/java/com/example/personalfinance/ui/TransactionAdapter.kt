package com.example.personalfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.TransactionType
import com.example.personalfinance.util.Formatters
import com.google.android.material.card.MaterialCardView

class TransactionAdapter(
    private val onEdit: (FinanceTransaction) -> Unit,
    private val onDelete: (FinanceTransaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val items = mutableListOf<FinanceTransaction>()

    fun submitList(data: List<FinanceTransaction>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TransactionViewHolder(
        itemView: View,
        private val onEdit: (FinanceTransaction) -> Unit,
        private val onDelete: (FinanceTransaction) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val container: MaterialCardView = itemView.findViewById(R.id.transactionCard)
        private val titleText: TextView = itemView.findViewById(R.id.transactionTitle)
        private val subtitleText: TextView = itemView.findViewById(R.id.transactionSubtitle)
        private val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        private val dateText: TextView = itemView.findViewById(R.id.transactionDate)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteTransactionButton)

        fun bind(item: FinanceTransaction) {
            titleText.text = item.category
            subtitleText.text = item.notes.ifBlank { "No note added" }
            amountText.text = Formatters.signedAmount(item.type, item.amount)
            dateText.text = Formatters.compactDate(item.date)

            val amountColor = if (item.type == TransactionType.INCOME) {
                R.color.income_green
            } else {
                R.color.expense_red
            }
            amountText.setTextColor(ContextCompat.getColor(itemView.context, amountColor))
            container.setOnClickListener { onEdit(item) }
            deleteButton.setOnClickListener { onDelete(item) }
        }
    }
}
