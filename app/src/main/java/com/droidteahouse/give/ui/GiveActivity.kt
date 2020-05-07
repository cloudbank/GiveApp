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


import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.droidteahouse.backdrop.BackDropIconClickListener
import com.droidteahouse.give.R
import com.droidteahouse.give.ServiceLocator
import kotlinx.android.synthetic.main.fragment_charity.*
import kotlinx.android.synthetic.main.main_layout.*


/**
 * A list activity that shows areas affected by corona virus
 *
 */
class GiveActivity : AppCompatActivity() {

    val model: CharityViewModel by viewModels { (ServiceLocator.instance(this)).provideViewModel(application, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        initMenu()
    }

    private fun initMenu() {
        setSupportActionBar(app_bar)
        val backdropListener = BackDropIconClickListener(
                this,
                product_grid,
                ContextCompat.getDrawable(this, R.drawable.shr_branded_menu), // Menu open icon
                ContextCompat.getDrawable(this, R.drawable.shr_close_menu))
        app_bar.setNavigationOnClickListener(backdropListener) // Menu close icon
        product_grid.setOnTouchListener(backdropListener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            product_grid.background = getDrawable(R.drawable.shr_product_grid_background_shape)
        }
    }

    fun updateData(v: View) {
        val nc = myNavHostFragment.findNavController()

        nc.navigate(R.id.charityFragment)
        var id = 1
        when (v.id) {
            R.id.animal -> id = 2
            R.id.medicalResearch -> id = 14
            R.id.internationalPeace -> id = 19
            R.id.homeless -> id = 28
            R.id.wildlife -> id = 1
        }
        changeCategory(id)
    }

    fun changeCategory(id: Int) {
        if (model.changeCategory(id)) {
            list.scrollToPosition(0)
            (list.adapter as? CharityAdapter)?.submitList(null)
        }
    }

}
