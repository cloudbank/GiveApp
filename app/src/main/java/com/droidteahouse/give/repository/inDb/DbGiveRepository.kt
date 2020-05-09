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


import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.toLiveData
import androidx.room.withTransaction
import com.droidteahouse.give.api.GiveApi
import com.droidteahouse.give.db.GiveDb
import com.droidteahouse.give.repository.GiveRepository
import com.droidteahouse.give.repository.Listing
import com.droidteahouse.give.repository.NetworkState
import com.droidteahouse.give.vo.Charity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
class DbGiveRepository(
        val db: GiveDb,
        private val coronaTrackerApi: GiveApi,
        private var boundaryCallback: GiveBoundaryCallback
) : GiveRepository {

    init {
        boundaryCallback.handleResponse = this::insertResultIntoDb
    }

    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 20
        private val TAG = DbGiveRepository::class.java.canonicalName
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private suspend fun updateResult(body: List<Charity>?) {
        var num = 0
        body?.let { it ->
            //@Transaction
            num = db.dao().updateCharities(it)
            Log.d(TAG, "Update on ${num} rows successful")
            boundaryCallback.incrementStart()
        }
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private suspend fun insertResultIntoDb(body: List<Charity>?) {
        body?.let { it ->
            db.withTransaction {

                val start = db.dao().getNextIndexInCategory(boundaryCallback.cause)
                val items = it.mapIndexed { index, child ->
                    child.indexInResponse = start + index
                    child
                }
                db.dao().insert(items)
            }
            boundaryCallback.incrementStart()
        }
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(mainScope: CoroutineScope, cateogryId: Int): LiveData<NetworkState> {
        //does this make sense?  check rate limiting/ throttling for this
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        boundaryCallback.resetStart()

        mainScope.launch {
            try {
                val api = async(Dispatchers.IO) { GiveApi.safeApiCall(null, networkState) { coronaTrackerApi.charities(categoryID = cateogryId, pageNum = boundaryCallback.page, pageSize = DEFAULT_NETWORK_PAGE_SIZE) } }
                val response = api.await()
                if (response != null) {
                    if (response.isSuccessful) {
                        val update = launch(Dispatchers.IO) { updateResult(response.body()) }
                        networkState.value = (NetworkState.LOADED)
                    } else {
                        networkState.value = (NetworkState.error(response.errorBody().toString()))
                    }
                }
            } catch (e: Exception) {
                networkState.value = (NetworkState.error(e.message))
            }
        }
        return networkState
    }

    /**
     * Returns a Listing for the given data
     *
     */
    @MainThread
    override suspend fun charities(pageSize: Int, ctx: CoroutineContext, scope: CoroutineScope, categoryId: Int): Listing<Charity> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        boundaryCallback.cause = categoryId
        boundaryCallback.scope = scope
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = refreshTrigger.switchMap {
            refresh(scope, categoryId)
        }

        return Listing(
                pagedList = db.dao().charities(categoryId).toLiveData(pageSize = pageSize, boundaryCallback = boundaryCallback),
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.helper.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }


}

