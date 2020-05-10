package com.droidteahouse.give.vo

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/*
Copyright (c) 2020 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */

@Entity(tableName = "charities",
        indices = [Index(value = ["ein"], unique = false)])

data class Charity(
        @field:Json(name = "charityNavigatorURL") val charityNavigatorURL: String,
        @field:Json(name = "mission") val mission: String,
        @field:Json(name = "websiteURL") val websiteURL: String,
        @field:Json(name = "tagLine") val tagLine: String,
        @field:Json(name = "charityName") val charityName: String?,
        @PrimaryKey
        @field:Json(name = "ein") val ein: Int?,
        @Embedded
        @field:Json(name = "currentRating") val currentRating: CurrentRating?,
        @Embedded
        @field:Json(name = "category") val category: Category,
        @Embedded
        @field:Json(name = "cause") val cause: Cause,

        @Embedded
        @field:Json(name = "mailingAddress") val mailingAddress: MailingAddress?,
        @Embedded
        @field:Json(name = "donationAddress") val donationAddress: DonationAddress?,
        @Embedded
        @field:Json(name = "advisories") val advisories: Advisories?,
        @Embedded
        @field:Json(name = "organization") val organization: Organization?
) {

    var indexInResponse: Int = -1
    var isFavorite: Boolean = false
}