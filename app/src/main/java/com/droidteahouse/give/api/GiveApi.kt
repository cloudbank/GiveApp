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

package com.droidteahouse.give.api


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingRequestHelper
import com.droidteahouse.give.repository.NetworkState
import com.droidteahouse.give.vo.Charity
import com.droidteahouse.give.vo.NewsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * API communication setup
 */
interface GiveApi {
    @GET("Organizations")
    suspend fun charities(@Query("rated") searchTerm: String = "true",
                          @Query("app_key") apiKey: String = APIKEY,
                          @Query("app_id") appId: String = APPID,
                          @Query("causeID") categoryID: Int,
                          @Query("pageSize") pageSize: Int,
                          @Query("pageNum") pageNum: Int): Response<List<Charity>>


    @GET("")
    suspend fun news(): Response<NewsResponse>


    companion object {
        val TAG = GiveApi::class.java.canonicalName
        private const val BASE_URL = "https://api.data.charitynavigator.org/v2/"
        private const val APIKEY = "6d3f97fd2e4edfbd6819dce425b8f6d5"
        private const val APPID = "2e38ea16"
        fun create(): GiveApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): GiveApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()


            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(GiveApi::class.java)
        }

        @JvmStatic
        suspend inline fun <T> safeApiCall(prh: PagingRequestHelper.Request.Callback?, networkState: LiveData<NetworkState>, crossinline responseFunction: suspend () -> T): T? {
            return try {
                val response = withContext(Dispatchers.IO) { responseFunction.invoke() }
                response
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    Log.e(TAG, "Call error: ${e.localizedMessage}", e.cause)
                    (networkState as MutableLiveData).postValue(NetworkState.error("Network error, please check your connection and try again"))
                    prh?.recordFailure(e)
                }
                null
            }
        }
    }


}