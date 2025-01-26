package com.example.fiestafy.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fiestafy.databinding.ItemAnnouncementBinding
import com.example.fiestafy.models.Announcement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnnouncementsAdapter : ListAdapter<Announcement, AnnouncementsAdapter.ViewHolder>(AnnouncementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.apply {
                announcementTitle.text = announcement.title
                announcementContent.text = announcement.content
                announcementDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(announcement.createdAt))
            }
        }
    }

    private class AnnouncementDiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement) = 
            oldItem == newItem
    }
} 