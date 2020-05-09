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

package com.droidteahouse.give.db


import androidx.paging.DataSource
import androidx.room.*
import com.droidteahouse.give.vo.Charity


@Dao
interface GiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<Charity>)

    @Transaction
    @Update
    suspend fun updateCharities(data: List<Charity>): Int


    @Query("SELECT * FROM charities WHERE causeID = :category ORDER BY indexInResponse ASC")
    fun charities(category: Int): DataSource.Factory<Int, Charity>

    //called from KSuspendingFunction
    @Query("SELECT MAX(indexInResponse) + 1 FROM charities WHERE causeID = :category")
    fun getNextIndexInCategory(category: Int): Int

}