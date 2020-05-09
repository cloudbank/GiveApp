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


import androidx.lifecycle.*
import com.droidteahouse.give.repository.GiveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CharityViewModel(
        private val repository: GiveRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val uiContext = viewModelScope.coroutineContext + Dispatchers.Main
    private val uiScope = CoroutineScope(uiContext)


    companion object {

        const val DEFAULT_CATEGORY = 2
        const val CATEGORY_KEY = "category"
    }

    init {
        if (!savedStateHandle.contains(CATEGORY_KEY)) {
            savedStateHandle.set(CATEGORY_KEY, DEFAULT_CATEGORY)
        }
    }

    private val repoResult = savedStateHandle.getLiveData<Int>(CATEGORY_KEY).switchMap {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(repository.charities(30, uiContext, uiScope, it))
        }
    }

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
        viewModelScope.launch {
            repoResult.value?.refresh?.invoke()
        }
    }

    fun retry() {
        repoResult.value?.retry?.invoke()
    }

}

