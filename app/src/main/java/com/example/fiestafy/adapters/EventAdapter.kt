package com.example.fiestafy.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fiestafy.R
import com.example.fiestafy.databinding.ItemEventBinding
import com.example.fiestafy.models.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.fiestafy.EventDetailActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.ContextCompat

class EventAdapter : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    private var events = listOf<Event>()
    private val auth = FirebaseAuth.getInstance()
    private var onItemClickListener: ((Event) -> Unit)? = null

    fun setOnItemClickListener(listener: (Event) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(private val binding: ItemEventBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventDescription.text = event.description
                eventLocation.text = event.location.name
                eventDate.text = formatDate(event.date)
                eventPrice.text = if (event.price > 0) {
                    "€${event.price}"
                } else {
                    "Gratis"
                }

                val userId = auth.currentUser?.uid
                val attendeeStatus = event.attendees[userId]
                paymentStatus.text = when {
                    event.price == 0.0 -> ""
                    attendeeStatus?.hasPaid == true -> "✓ Pagado"
                    attendeeStatus != null -> "⚠ Pendiente de pago"
                    else -> ""
                }
                paymentStatus.setTextColor(
                    if (attendeeStatus?.hasPaid == true) 
                        ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
                    else 
                        ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark)
                )

                Glide.with(itemView)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.event_placeholder)
                    .into(eventImage)

                root.setOnClickListener {
                    val intent = Intent(itemView.context, EventDetailActivity::class.java)
                    intent.putExtra("eventId", event.id)
                    itemView.context.startActivity(intent)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(timestamp))
        }
    }
} 