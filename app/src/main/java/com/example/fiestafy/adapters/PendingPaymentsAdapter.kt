package com.example.fiestafy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fiestafy.databinding.ItemPendingPaymentBinding
import com.example.fiestafy.models.Event
import java.text.SimpleDateFormat
import java.util.*

class PendingPaymentsAdapter(
    private val onPayClick: (Event) -> Unit
) : ListAdapter<Event, PendingPaymentsAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPendingPaymentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentViewHolder(
        private val binding: ItemPendingPaymentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventDate.text = formatDate(event.date)
                eventPrice.text = "€${event.price}"
                
                payButton.setOnClickListener {
                    onPayClick(event)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event) = 
        oldItem.id == newItem.id
    
    override fun areContentsTheSame(oldItem: Event, newItem: Event) = 
        oldItem == newItem
} 