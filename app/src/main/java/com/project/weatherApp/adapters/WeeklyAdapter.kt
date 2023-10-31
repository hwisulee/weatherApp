package com.project.weatherApp.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.weatherApp.R
import com.project.weatherApp.databinding.ItemviewWeeklyBinding
import com.project.weatherApp.domains.Weekly
import com.project.weatherApp.utils.TimeMaker

class WeeklyAdapter(items: ArrayList<Weekly>) : ListAdapter<Weekly, WeeklyAdapter.ViewHolder>(diffUtil) {
    val items: ArrayList<Weekly> = items

    inner class ViewHolder(private val binding: ItemviewWeeklyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Weekly) {
            with(binding) {
                daytxt.text = data.day
                status.text = data.status
                high.text = "${data.highTemper}º"
                low.text = "${data.lowTemper}º"

                val drawableResourceId = imageView.resources.getIdentifier(items[adapterPosition].picPath, "drawable", root.context.packageName)
                Glide.with(binding.root.context).load(drawableResourceId).into(imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyAdapter.ViewHolder {
        val view = ItemviewWeeklyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyAdapter.ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<Weekly>() {
            override fun areItemsTheSame(oldItem: Weekly, newItem: Weekly): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: Weekly, newItem: Weekly): Boolean = oldItem == newItem
        }
    }
}