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


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.droidteahouse.GlideRequests
import com.droidteahouse.give.R
import com.droidteahouse.give.vo.Charity


/**
 * A RecyclerView ViewHolder that displays an area
 */
class CharityViewHolder(view: View, private val glide: GlideRequests
) : RecyclerView.ViewHolder(view) {

    private val name: TextView = view.findViewById(R.id.name)
    private val missionStatement: TextView = view.findViewById(R.id.missionStatement)

    private val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
    private val ratings: ImageView = view.findViewById(R.id.ratings)

    private var charity: Charity? = null


    fun bind(charity: Charity?) {
        this.charity = charity
        name.text = charity?.charityName

        glide.load(charity?.currentRating?.ratingImage?.small)
                .fitCenter()
                .placeholder(android.R.drawable.btn_star)
                .into(ratings)

        glide.load(imageForCause(charity?.cause!!.causeID))
                .fitCenter()
                .placeholder(android.R.drawable.btn_star)
                .into(thumbnail)

        missionStatement.text = charity?.mission


    }

    private fun imageForCause(cause: Int): Int {
        var image = R.drawable.ic_pets_24px
        when (cause) {
            2 -> image = (R.drawable.ic_pets_24px)
            19 -> image = (R.drawable.ic_sign)
            14 -> image = (R.drawable.ic_healthcare_and_medical_2)
            28 -> image = (R.drawable.ic_ic_home_black_48dp)
            1 -> image = (R.drawable.ic_seeding)
        }
        return image
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): CharityViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.charity_item, parent, false)
            return CharityViewHolder(view, glide)
        }
    }

    fun updateCases(item: Charity?) {

    }
}