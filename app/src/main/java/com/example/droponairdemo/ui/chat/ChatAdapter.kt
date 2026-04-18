package com.example.droponairdemo.ui.chat

import android.graphics.Color
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val items: List<ChatItem>,
    private val myUserId: String,
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class VH(val root: LinearLayout) : RecyclerView.ViewHolder(root) {
        val tvSender:  TextView = TextView(root.context).also { it.textSize = 11f; it.alpha = .6f }
        val tvText:    TextView = TextView(root.context).also { it.textSize = 15f }
        val tvTime:    TextView = TextView(root.context).also { it.textSize = 10f; it.alpha = .6f }
        init { root.addView(tvSender); root.addView(tvText); root.addView(tvTime) }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val ll = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(16, 4, 16, 4) }
            setPadding(16, 8, 16, 8)
        }
        return VH(ll)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        val isSelf = item.fromUserId == myUserId

        h.tvSender.visibility = if (isSelf) View.GONE else View.VISIBLE
        h.tvSender.text = item.fromUserId
        h.tvText.text   = item.text
        h.tvTime.text   = timeFmt.format(Date(item.timestamp))

        // Style: self = blue, other = grey
        h.root.setBackgroundColor(if (isSelf) Color.parseColor("#3880ff") else Color.parseColor("#f0f0f0"))
        h.tvText.setTextColor(if (isSelf) Color.WHITE else Color.BLACK)
        h.tvTime.setTextColor(if (isSelf) Color.WHITE else Color.BLACK)

        // Align right for self
        (h.root.layoutParams as RecyclerView.LayoutParams).also {
            if (isSelf) it.leftMargin = 120 else it.rightMargin = 120
        }
    }
}
