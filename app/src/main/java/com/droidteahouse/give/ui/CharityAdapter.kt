/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidteahouse.give.ui

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.droidteahouse.GlideRequests
import com.droidteahouse.give.R
import com.droidteahouse.give.repository.NetworkState
import com.droidteahouse.give.vo.Charity


/**
 * A paged list adapter implementation for a charity
 */
class CharityAdapter(
        private val glide: GlideRequests,
        private val retryCallback: () -> Unit)
    : PagedListAdapter<Charity, RecyclerView.ViewHolder>(DIFF_COMPARATOR) {
    private var networkState: NetworkState? = null


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.charity_item -> (holder as CharityViewHolder).bind(getItem(position))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(
                    networkState)
        }
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as CharityViewHolder).updateCases(item)
        } else {
            onBindViewHolder(holder, position)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.charity_item -> CharityViewHolder.create(parent, glide)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.charity_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        private val PAYLOAD_SCORE = Any()
        val DIFF_COMPARATOR = object : DiffUtil.ItemCallback<Charity>() {
            override fun areContentsTheSame(oldItem: Charity, newItem: Charity): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: Charity, newItem: Charity): Boolean =
                    oldItem.ein == newItem.ein

            override fun getChangePayload(oldItem: Charity, newItem: Charity) {

            }
        }

    }
}
