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

package com.droidteahouse.give

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.droidteahouse.give.api.GiveApi
import com.droidteahouse.give.db.GiveDb
import com.droidteahouse.give.repository.GiveRepository
import com.droidteahouse.give.repository.inDb.DbGiveRepository
import com.droidteahouse.give.repository.inDb.GiveBoundaryCallback
import com.droidteahouse.give.ui.CharityViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                            app = context.applicationContext as Application
                    )
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(): GiveRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getGiveApi(): GiveApi

    fun provideViewModel(instance: Application, owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory

}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application) : ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy {
        GiveDb.create(app)
    }

    private val api by lazy {
        GiveApi.create()
    }

    val boundaryCallback = GiveBoundaryCallback(
            webservice = getGiveApi(),
            ioExecutor = getDiskIOExecutor(),
            networkPageSize = 10)

    override fun getRepository(): GiveRepository {
        return DbGiveRepository(
                db = db,
                giveApi = getGiveApi(),
                boundaryCallback = boundaryCallback)
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getGiveApi(): GiveApi = api


    override fun provideViewModel(instance: Application, owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {


        val factory = object : AbstractSavedStateViewModelFactory(owner, null) {

            override fun <CharityViewModel : ViewModel> create(
                    key: String,
                    modelClass: Class<CharityViewModel>,
                    handle: SavedStateHandle
            ): CharityViewModel {
                return CharityViewModel(getRepository(), handle) as CharityViewModel
            }
        }
        return factory
    }
}





