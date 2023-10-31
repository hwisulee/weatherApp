package com.project.weatherApp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.weatherApp.R
import com.project.weatherApp.databinding.ItemviewHourlyBinding
import com.project.weatherApp.domains.Hourly
import com.project.weatherApp.utils.TimeMaker

class HourlyAdapter(items: ArrayList<Hourly>) : ListAdapter<Hourly, HourlyAdapter.ViewHolder>(diffUtil) {
    val items: ArrayList<Hourly> = items

    inner class ViewHolder(private val binding: ItemviewHourlyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Hourly) {
            with(binding) {
                hourtxt.text = data.hour
                tempertxt.text = "${data.temper}º"
                val drawableResourceId = imageView.resources.getIdentifier(items[adapterPosition].picPath, "drawable", root.context.packageName)
                Glide.with(binding.root.context).load(drawableResourceId).into(imageView)

                when(TimeMaker().getSetting("")[0]) {
                    "morning", "evening" -> {
                        cardView.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_evening_box1)
                        cardInner.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_evening_box1)
                    }
                    "afternoon" -> {
                        cardView.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_afternoon_box1)
                        cardInner.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_afternoon_box1)
                    }
                    else -> {
                        cardView.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_night_box1)
                        cardInner.background = ContextCompat.getDrawable(root.context.applicationContext, R.drawable.background_night_box1)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyAdapter.ViewHolder {
        val view = ItemviewHourlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyAdapter.ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<Hourly>() {
            override fun areItemsTheSame(oldItem: Hourly, newItem: Hourly): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: Hourly, newItem: Hourly): Boolean = oldItem == newItem
        }
    }
}