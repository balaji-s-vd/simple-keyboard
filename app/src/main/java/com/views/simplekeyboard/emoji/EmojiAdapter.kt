package com.views.simplekeyboard.emoji

import android.view.Gravity
import android.view.ViewGroup
import androidx.emoji2.widget.EmojiTextView
import androidx.recyclerview.widget.RecyclerView


class EmojiAdapter(
    private val emojis: List<String>,
    private val onEmojiClick: (String) -> Unit
): RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {
    inner class EmojiViewHolder(val emojiTextView: EmojiTextView) : RecyclerView.ViewHolder(emojiTextView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmojiViewHolder {
        val view = EmojiTextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(120, 120)
            textSize = 24f
            gravity = Gravity.CENTER
        }
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: EmojiViewHolder,
        position: Int
    ) {
        val emoji = emojis[position]
        holder.emojiTextView.text = emoji
        holder.emojiTextView.setOnClickListener { onEmojiClick(emoji) }
    }

    override fun getItemCount(): Int {
        return emojis.size
    }
}