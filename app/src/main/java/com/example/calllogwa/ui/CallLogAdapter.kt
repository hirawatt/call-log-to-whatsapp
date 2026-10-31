package com.example.calllogwa.ui

import android.content.Context
import android.provider.CallLog
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calllogwa.R
import com.example.calllogwa.data.CallEntry
import com.example.calllogwa.databinding.ItemCallEntryBinding
import com.example.calllogwa.util.WhatsAppLauncher
import java.util.Date

class CallLogAdapter(
    private val onWhatsAppClick: (CallEntry) -> Unit
) : ListAdapter<CallEntry, CallLogAdapter.CallLogViewHolder>(CallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val binding = ItemCallEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CallLogViewHolder(private val binding: ItemCallEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: CallEntry) {
            val context = binding.root.context

            // Title and Number
            binding.tvName.text = entry.displayTitle
            binding.tvNumber.text = entry.formattedNumber

            // Initials Avatar
            val initials = getInitials(entry.displayTitle)
            binding.tvAvatarInitials.text = initials

            // Call Type Icon & Color
            when (entry.callType) {
                CallLog.Calls.INCOMING_TYPE -> {
                    binding.ivCallType.setImageResource(R.drawable.ic_call_incoming)
                }
                CallLog.Calls.OUTGOING_TYPE -> {
                    binding.ivCallType.setImageResource(R.drawable.ic_call_outgoing)
                }
                CallLog.Calls.MISSED_TYPE, CallLog.Calls.REJECTED_TYPE -> {
                    binding.ivCallType.setImageResource(R.drawable.ic_call_missed)
                }
                else -> {
                    binding.ivCallType.setImageResource(R.drawable.ic_call_incoming)
                }
            }

            // Date & Duration Text
            val typeStr = when (entry.callType) {
                CallLog.Calls.INCOMING_TYPE -> context.getString(R.string.incoming_call)
                CallLog.Calls.OUTGOING_TYPE -> context.getString(R.string.outgoing_call)
                CallLog.Calls.MISSED_TYPE, CallLog.Calls.REJECTED_TYPE -> context.getString(R.string.missed_call)
                else -> ""
            }
            val dateStr = DateFormat.format("MMM dd, h:mm a", Date(entry.date)).toString()
            val durationStr = formatDuration(entry.duration)

            binding.tvCallMeta.text = buildString {
                append(typeStr)
                append(" • ")
                append(dateStr)
                if (durationStr.isNotBlank()) {
                    append(" • ")
                    append(durationStr)
                }
            }

            // WhatsApp Business Badge
            if (!entry.waBusinessName.isNullOrBlank()) {
                binding.tvWaBusinessBadge.visibility = View.VISIBLE
                binding.tvWaBusinessBadge.text = "WA Business: ${entry.waBusinessName}"
            } else {
                binding.tvWaBusinessBadge.visibility = View.GONE
            }

            // WhatsApp Button Visibility
            if (entry.hasWhatsApp) {
                binding.btnWhatsApp.visibility = View.VISIBLE
                binding.btnWhatsApp.setOnClickListener {
                    onWhatsAppClick(entry)
                }
            } else {
                binding.btnWhatsApp.visibility = View.GONE
                binding.btnWhatsApp.setOnClickListener(null)
            }
        }

        private fun getInitials(name: String): String {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return "#"
            val parts = trimmed.split(" ").filter { it.isNotBlank() }
            return if (parts.size >= 2) {
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            } else if (parts.isNotEmpty() && parts[0].first().isLetter()) {
                "${parts[0].first().uppercaseChar()}"
            } else {
                "#"
            }
        }

        private fun formatDuration(durationSeconds: Long): String {
            if (durationSeconds <= 0) return ""
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return if (mins > 0) {
                "${mins}m ${secs}s"
            } else {
                "${secs}s"
            }
        }
    }

    private class CallDiffCallback : DiffUtil.ItemCallback<CallEntry>() {
        override fun areItemsTheSame(oldItem: CallEntry, newItem: CallEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CallEntry, newItem: CallEntry): Boolean {
            return oldItem == newItem
        }
    }
}
