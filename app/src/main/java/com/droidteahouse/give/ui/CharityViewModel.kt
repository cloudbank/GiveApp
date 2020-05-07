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


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.droidteahouse.give.repository.GiveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class CharityViewModel(
        private val repository: GiveRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)


    companion object {

        const val DEFAULT_CATEGORY = 2
        const val CATEGORY_KEY = "category"
    }

    init {
        if (!savedStateHandle.contains(CATEGORY_KEY)) {
            savedStateHandle.set(CATEGORY_KEY, DEFAULT_CATEGORY)
        }
    }

    private val repoResult = savedStateHandle.getLiveData<Int>(CATEGORY_KEY).map {
        repository.charities(30, ioScope, uiScope, it)
    }

    //livedata vs databinding
    //a livedata that observes the value of the button in the menu
    //mapping that to a repo call
    //this vm
    val resultList = repoResult.switchMap { it.pagedList }
    val networkState = repoResult.switchMap { it.networkState }
    val refreshState = repoResult.switchMap { it.refreshState }


    fun changeCategory(category: Int): Boolean {
        if (savedStateHandle.get<Int>(CATEGORY_KEY) == category) {
            return false
        }
        savedStateHandle.set(CATEGORY_KEY, category)
        return true
    }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun retry() {
        repoResult.value?.retry?.invoke()
    }

    override fun onCleared() {
        viewModelJob.cancel()
        super.onCleared()

    }
}

