package com.example.fiestafy.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fiestafy.databinding.ItemCalendarEventBinding
import com.example.fiestafy.models.Event
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, CalendarEventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemCalendarEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(event.date))
                eventLocation.text = event.location.name
                root.setOnClickListener { onEventClick(event) }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Event, newItem: Event) = 
            oldItem == newItem
    }
} 