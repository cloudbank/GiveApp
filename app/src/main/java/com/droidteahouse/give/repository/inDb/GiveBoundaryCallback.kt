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

package com.droidteahouse.give.repository.inDb

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import com.droidteahouse.give.api.GiveApi
import com.droidteahouse.give.repository.NetworkState
import com.droidteahouse.give.util.createStatusLiveData
import com.droidteahouse.give.vo.Charity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.concurrent.Executor
import kotlin.reflect.KFunction1

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class GiveBoundaryCallback(
        private val webservice: GiveApi,
        private val ioExecutor: Executor,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Charity>() {
    lateinit var scope: CoroutineScope
    lateinit var handleResponse: KFunction1<@ParameterName(name = "body") List<Charity>?, Unit>
    val helper = PagingRequestHelper(ioExecutor)
    var networkState = helper.createStatusLiveData() as MutableLiveData


    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 20
    }

    var page = 1
        get() = field
    var cause: Int = 2

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        resetStart()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            scope.launch {
                try {
                    val api = async(Dispatchers.IO) { GiveApi.safeApiCall(it, networkState) { webservice.charities(categoryID = cause, pageNum = page, pageSize = DEFAULT_NETWORK_PAGE_SIZE) } }
                    val response = api.await()
                    if (response != null) {
                        if (response.isSuccessful) launch(Dispatchers.IO) { insertItemsIntoDb(response, it) } else it.recordFailure(Throwable(response.errorBody().toString()))
                    }
                } catch (e: Exception) {
                    networkState.value = (NetworkState.error(e.message ?: "unknown err"))
                    it.recordFailure(e)
                }

            }
        }
    }

    /**
     *
     */
    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: Charity) {
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Charity) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            scope.launch {
                try {
                    val api = async(Dispatchers.IO) { GiveApi.safeApiCall(it, networkState) { webservice.charities(categoryID = itemAtEnd.cause.causeID, pageNum = page, pageSize = DEFAULT_NETWORK_PAGE_SIZE) } }
                    val response = api.await()
                    if (response != null) {
                        if (response.isSuccessful) launch(Dispatchers.IO) { insertItemsIntoDb(response, it) } else it.recordFailure(Throwable(response.errorBody().toString()))
                    }
                } catch (e: Exception) {
                    networkState.value = (NetworkState.error(e.message ?: "unknown err"))
                    it.recordFailure(e)
                }

            }
        }


    }


    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<List<Charity>>,
            it: PagingRequestHelper.Request.Callback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleResponse?.invoke(response.body())
                it.recordSuccess()
            } catch (e: Exception) {
                it.recordFailure(e)
            }
        }
    }

    fun incrementStart() {
        page++
    }

    fun resetStart() {
        page = 1
    }

}