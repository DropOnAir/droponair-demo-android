package com.example.droponairdemo.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import com.droponair.sdk.DropOnAir
import com.droponair.sdk.DropOnAirListener
import com.droponair.sdk.model.CallEvent
import com.droponair.sdk.model.Group
import com.droponair.sdk.model.Message
import com.droponair.sdk.model.MessageDelete
import com.droponair.sdk.model.MessageEdit
import com.droponair.sdk.model.GroupMessage
import com.droponair.sdk.model.GroupCallEvent
import com.example.droponairdemo.R
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatItem>()
    private lateinit var adapter: ChatAdapter
    private lateinit var myUserId: String
    private var activeCallId: String? = null
    private var activeGroupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        myUserId = intent.getStringExtra("userId") ?: "me"
        title = "Chat, $myUserId"

        val rv         = findViewById<RecyclerView>(R.id.recyclerView)
        val etTo       = findViewById<EditText>(R.id.etToUserId)
        val etMsg      = findViewById<EditText>(R.id.etMessage)
        val btnSend    = findViewById<Button>(R.id.btnSend)
        val btnCall    = findViewById<Button>(R.id.btnCall)
        val btnGroups  = findViewById<Button>(R.id.btnGroups)
        val tvStatus   = findViewById<TextView>(R.id.tvStatus)

        adapter = ChatAdapter(messages, myUserId) { pos -> showMessageActions(pos) }
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        // Register SDK listener
        DropOnAir.getInstance().setListener(object : DropOnAirListener {
            override fun onConnected() {
                tvStatus.text = "🟢 Connected"
                tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            }

            override fun onDisconnected(reason: String?, willReconnect: Boolean) {
                tvStatus.text = if (willReconnect) "🟡 Reconnecting…" else "🔴 Offline"
            }

            override fun onMessageReceived(message: Message) {
                messages.add(ChatItem(message.messageId, message.fromUserId, message.text, message.timestamp, isSelf = false))
                adapter.notifyItemInserted(messages.lastIndex)
                rv.scrollToPosition(messages.lastIndex)
            }

            override fun onMessageEdit(edit: MessageEdit) {
                runOnUiThread {
                    val idx = messages.indexOfFirst { it.id == edit.originalMessageId }
                    if (idx >= 0) {
                        val prev = messages[idx]
                        messages[idx] = prev.copy(text = edit.plaintext, edited = true, deleted = false)
                        adapter.notifyItemChanged(idx)
                    }
                }
            }

            override fun onMessageDelete(delete: MessageDelete) {
                runOnUiThread {
                    val idx = messages.indexOfFirst { it.id == delete.originalMessageId }
                    if (idx >= 0) {
                        val prev = messages[idx]
                        messages[idx] = prev.copy(text = "(message deleted)", deleted = true, edited = false)
                        adapter.notifyItemChanged(idx)
                    }
                }
            }

            override fun onError(error: Throwable) {
                tvStatus.text = "⚠ ${error.message}"
            }

            override fun onCallEvent(event: CallEvent) {
                runOnUiThread {
                    when (event.type) {
                        "CALL_INVITE" -> {
                            activeCallId = event.callId
                            AlertDialog.Builder(this@ChatActivity)
                                .setTitle("Incoming Call")
                                .setMessage("Call from ${event.targetUserId}")
                                .setPositiveButton("Accept") { _, _ ->
                                    lifecycleScope.launch {
                                        DropOnAir.getInstance().acceptCall(event.callId!!)
                                        tvStatus.text = "📞 In call: ${event.callId}"
                                    }
                                }
                                .setNegativeButton("Reject") { _, _ ->
                                    lifecycleScope.launch {
                                        DropOnAir.getInstance().rejectCall(event.callId!!)
                                        activeCallId = null
                                    }
                                }
                                .setCancelable(false)
                                .show()
                        }
                        "CALL_ACCEPTED" -> tvStatus.text = "📞 Call active: ${event.callId}"
                        "CALL_RINGING"  -> tvStatus.text = "📞 Ringing…"
                        "CALL_ENDED", "CALL_REJECTED", "CALL_CANCELLED" -> {
                            tvStatus.text = "🟢 Connected"
                            activeCallId = null
                        }
                        "CALL_DENIED_LIMIT_REACHED" -> {
                            tvStatus.text = "⚠ Call limit reached"
                            activeCallId = null
                        }
                    }
                }
            }

            override fun onGroupMessageReceived(message: GroupMessage) {
                runOnUiThread {
                    messages.add(ChatItem(message.messageId, message.fromUserId, message.text ?: "", message.timestamp, isSelf = false, groupId = message.groupId))
                    adapter.notifyItemInserted(messages.lastIndex)
                    rv.scrollToPosition(messages.lastIndex)
                }
            }

            override fun onGroupMessageAcknowledged(messageId: String, groupId: String, ackType: String) {}
            override fun onGroupCallEvent(event: GroupCallEvent) {}
        })

        btnSend.setOnClickListener {
            val to  = etTo.text.toString().trim()
            val txt = etMsg.text.toString().trim()
            if (to.isEmpty() || txt.isEmpty()) return@setOnClickListener

            etMsg.text.clear()
            lifecycleScope.launch {
                try {
                    val messageId = DropOnAir.getInstance().sendMessage(to, txt)
                    messages.add(ChatItem(messageId, myUserId, txt, System.currentTimeMillis(), isSelf = true, toUserId = to))
                    adapter.notifyItemInserted(messages.lastIndex)
                    rv.scrollToPosition(messages.lastIndex)
                } catch (e: Exception) {
                    tvStatus.text = "⚠ Send error: ${e.message}"
                }
            }
        }

        btnCall.setOnClickListener {
            val to = etTo.text.toString().trim()
            if (to.isEmpty()) return@setOnClickListener

            if (activeCallId != null) {
                // End active call
                lifecycleScope.launch {
                    DropOnAir.getInstance().endCall(activeCallId!!)
                    activeCallId = null
                    tvStatus.text = "🟢 Connected"
                    btnCall.text = "📞"
                }
            } else {
                lifecycleScope.launch {
                    try {
                        val callId = DropOnAir.getInstance().startCall(to)
                        activeCallId = callId
                        tvStatus.text = "📞 Calling $to…"
                        btnCall.text = "🔴"
                    } catch (e: Exception) {
                        tvStatus.text = "⚠ Call error: ${e.message}"
                    }
                }
            }
        }

        btnGroups.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val groups = DropOnAir.getInstance().getGroups()
                    val names = groups.map { "${it.name ?: it.groupId} (${it.members.size} members)" }.toTypedArray()
                    val ids = groups.map { it.groupId }
                    AlertDialog.Builder(this@ChatActivity)
                        .setTitle("Groups")
                        .setItems(names) { _, which ->
                            activeGroupId = ids[which]
                            tvStatus.text = "Group: ${names[which]}"
                        }
                        .setPositiveButton("Create") { _, _ -> showCreateGroupDialog(tvStatus) }
                        .setNeutralButton("Send to Group") { _, _ ->
                            activeGroupId?.let { gid ->
                                val txt = etMsg.text.toString().trim()
                                if (txt.isNotEmpty()) {
                                    etMsg.text.clear()
                                    lifecycleScope.launch {
                                        try {
                                            DropOnAir.getInstance().sendCleartextGroupMessage(gid, txt)
                                            messages.add(ChatItem(System.currentTimeMillis().toString(), myUserId, txt, System.currentTimeMillis(), isSelf = true, groupId = gid))
                                            adapter.notifyItemInserted(messages.lastIndex)
                                            rv.scrollToPosition(messages.lastIndex)
                                        } catch (e: Exception) {
                                            tvStatus.text = "⚠ Group send error: ${e.message}"
                                        }
                                    }
                                }
                            }
                        }
                        .show()
                } catch (e: Exception) {
                    tvStatus.text = "⚠ Groups error: ${e.message}"
                }
            }
        }
    }

    private fun showCreateGroupDialog(tvStatus: TextView) {
        val input = EditText(this).apply { hint = "Group name" }
        AlertDialog.Builder(this)
            .setTitle("Create Group")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val group = DropOnAir.getInstance().createGroup(name, emptyList())
                            activeGroupId = group.groupId
                            tvStatus.text = "Created group: ${group.name}"
                        } catch (e: Exception) {
                            tvStatus.text = "⚠ Create error: ${e.message}"
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        DropOnAir.getInstance().setListener(null)
    }

    private fun showMessageActions(position: Int) {
        val item = messages.getOrNull(position) ?: return
        if (!item.isSelf || item.deleted) return
        val to = item.toUserId
        if (to.isNullOrEmpty() || item.groupId != null) return

        AlertDialog.Builder(this)
            .setTitle("Message")
            .setItems(arrayOf("Edit", "Delete for everyone", "Delete for me")) { _, which ->
                when (which) {
                    0 -> promptEditMessage(item.id, to, item.text)
                    1 -> performDelete(item.id, to, "FOR_EVERYONE")
                    2 -> performDelete(item.id, to, "FOR_ME")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptEditMessage(messageId: String, toUserId: String, currentText: String) {
        val input = EditText(this).apply { setText(currentText) }
        AlertDialog.Builder(this)
            .setTitle("Edit message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val next = input.text.toString().trim()
                if (next.isEmpty() || next == currentText) return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        DropOnAir.getInstance().editMessage(messageId, toUserId, next)
                        val idx = messages.indexOfFirst { it.id == messageId }
                        if (idx >= 0) {
                            messages[idx] = messages[idx].copy(text = next, edited = true, deleted = false)
                            adapter.notifyItemChanged(idx)
                        }
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@ChatActivity).setMessage("Edit failed: ${e.message}").show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(messageId: String, toUserId: String, scope: String) {
        lifecycleScope.launch {
            try {
                DropOnAir.getInstance().deleteMessage(messageId, toUserId, scope)
                val idx = messages.indexOfFirst { it.id == messageId }
                if (idx >= 0) {
                    messages[idx] = messages[idx].copy(text = "(message deleted)", deleted = true, edited = false)
                    adapter.notifyItemChanged(idx)
                }
            } catch (e: Exception) {
                AlertDialog.Builder(this@ChatActivity).setMessage("Delete failed: ${e.message}").show()
            }
        }
    }
}

data class ChatItem(
    val id: String,
    val fromUserId: String,
    val text: String,
    val timestamp: Long,
    val isSelf: Boolean,
    val groupId: String? = null,
    val toUserId: String? = null,
    val edited: Boolean = false,
    val deleted: Boolean = false,
)
