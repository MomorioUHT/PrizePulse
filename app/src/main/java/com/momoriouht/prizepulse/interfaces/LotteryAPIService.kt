package com.momoriouht.prizepulse.interfaces

import retrofit2.Call
import retrofit2.http.POST
import com.momoriouht.prizepulse.model.LotteryResponse

interface LotteryAPIService {
    @POST("getLatestLottery")
    fun getLatestLottery(): Call<LotteryResponse>
}