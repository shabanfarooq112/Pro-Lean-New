package com.horizam.pro.elean.ui.main.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.model.response.Order
import com.horizam.pro.elean.databinding.ItemActiveOrderBinding
import com.horizam.pro.elean.ui.main.callbacks.OnItemClickListener

import com.horizam.pro.elean.utils.BaseUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CompletedSalesAdapter(val listener: OnItemClickListener) :
    ListAdapter<Order, CompletedSalesAdapter.DataViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            ItemActiveOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DataViewHolder(private val binding: ItemActiveOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val review = getItem(position)
                    if (review != null) {
                        listener.onItemClick(review)
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(order: Order) {
            binding.apply {
                tvUserName.text = order.username
                tvDate.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM-dd-yyyy"))
                tvDescription.text = order.description
                tvPrice.text = order.amount.toString().plus(order.currency)
                tvStatus.text = itemView.context.getString(R.string.str_completed)
                Glide.with(itemView)
                    .load(Constants.BASE_URL.plus(order.image))
                    .error(R.drawable.bg_splash)
                    .into(ivUser)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem.orderNo == newItem.orderNo
            }

            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem == newItem
            }

        }
    }
}