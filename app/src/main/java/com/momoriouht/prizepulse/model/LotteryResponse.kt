package com.momoriouht.prizepulse.model

data class LotteryResponse(
    val statusMessage: String,
    val statusCode: Int,
    val status: Boolean,
    val response: LotteryContent,
)

data class LotteryContent(
    val data: LotteryData,
    val displayDate: DisplayDate
)

data class LotteryData(
    val first: Prize,
    val second: Prize,
    val third: Prize,
    val fourth: Prize,
    val fifth: Prize,
    val last2: Prize,
    val last3f: Prize,
    val last3b: Prize,
    val near1: Prize
)

data class Prize(
    val price: String,
    val number: List<PrizeNumber>
)

data class PrizeNumber(
    val round: Int,
    val value: String
)

data class DisplayDate (
    val date: String,
    val month: String,
    val year: String
)