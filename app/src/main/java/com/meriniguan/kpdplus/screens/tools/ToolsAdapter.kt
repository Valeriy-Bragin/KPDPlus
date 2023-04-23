package com.meriniguan.kpdplus.screens.tools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.data.room.Tool
import com.meriniguan.kpdplus.databinding.ItemToolBinding

class ToolsAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Tool, ToolsAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemToolBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    interface OnItemClickListener {
        fun onItemClick(tool: Tool)
    }

    inner class TasksViewHolder(private val binding: ItemToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
            }
        }

        fun bind(tool: Tool) {
            binding.apply {
                textViewName.text = tool.name
                if (tool.holderName.isNotEmpty()) {
                    textViewHolds.visibility = View.VISIBLE
                    textViewHolderName.visibility = View.VISIBLE
                    textViewHolderName.text = tool.holderName
                } else {
                    textViewHolds.visibility = View.INVISIBLE
                    textViewHolderName.visibility = View.INVISIBLE
                }
                if(tool.hasPhoto()) {
                    Glide.with(root.context)
                        .load(tool.photoUri)
                    .into(imageViewTool)
                } else {
                    imageViewTool.setImageResource(R.drawable.default_image)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Tool>() {

        override fun areItemsTheSame(oldItem: Tool, newItem: Tool): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Tool, newItem: Tool): Boolean =
            oldItem == newItem
    }
}