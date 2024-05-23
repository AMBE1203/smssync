package com.example.dtclnh.presentation.page.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dtclnh.R
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.presentation.base.ext.generateUniqueID

class SmsAdapter : ListAdapter<SmsModel, SmsAdapter.SmsViewHolder>(SmsDiffCallback()) {

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.tvSender)
        val idTextView: TextView = itemView.findViewById(R.id.tvId)
        val contentTextView: TextView = itemView.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = getItem(position)
        holder.senderTextView.text = "Sender: ${sms.sender}"
        holder.idTextView.text = "Id: ${generateUniqueID(sms.receivedAt.toLong(), sms.sender, sms.smsId)}"
        holder.contentTextView.text = "Content:\n ${sms.content}"
    }
}


class SmsDiffCallback : DiffUtil.ItemCallback<SmsModel>() {
    override fun areItemsTheSame(oldItem: SmsModel, newItem: SmsModel): Boolean {
        return oldItem.smsId == newItem.smsId
    }

    override fun areContentsTheSame(oldItem: SmsModel, newItem: SmsModel): Boolean {
        return oldItem == newItem
    }
}
