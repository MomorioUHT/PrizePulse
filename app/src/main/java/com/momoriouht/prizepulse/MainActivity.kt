package com.momoriouht.prizepulse

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.momoriouht.prizepulse.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.momoriouht.prizepulse.model.LotteryResponse
import com.momoriouht.prizepulse.interfaces.LotteryAPIService
import com.momoriouht.prizepulse.model.DisplayDate

class MainActivity : AppCompatActivity() {

    final val PREF_NAME = "task_list"
    final val TASK_LIST_PREF_KEY = "items"

    lateinit var binding: ActivityMainBinding
    val numbersToCheck = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null

    private var firstPrizeNumbers = listOf<String>()
    private var secondPrizeNumbers = listOf<String>()
    private var thirdPrizeNumbers = listOf<String>()
    private var fourthPrizeNumbers = listOf<String>()
    private var fifthPrizeNumbers = listOf<String>()

    //Special
    private var lastTwoPrizeNumbers = listOf<String>()
    private var lastThreePrizeNumbers = listOf<String>()
    private var firstThreePrizeNumbers = listOf<String>()
    //--
    private var nearFirstPrizeNumbers = listOf<String>()

    //Task Schedule ===============================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, numbersToCheck)
        binding.sampleList1.adapter = adapter

        restoreTaskList()
        fetchLotteryData()
    }

    override fun onStop() {
        super.onStop()
        saveTaskList()
    }

    private fun saveTaskList() {
        val builder = StringBuilder()
        for (i in numbersToCheck) {
            builder.append(i)
            builder.append(",")
        }
        val data = builder.toString()

        val preference = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString(TASK_LIST_PREF_KEY, data)
        editor.commit()
    }

    private fun restoreTaskList() {
        val preference = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = preference.getString(TASK_LIST_PREF_KEY, null)
        if (data != null) {
            //Put data back to List if restored data is not null
            val strArray = data.split(",")
            for (i in strArray) {
                if (i != "") {
                    adapter?.add(i)
                }
            }
        }
    }

    //Task Schedule ===============================================================================

    fun displayCmds(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        binding.cmdText.text = msg
    }

    fun addNumberToArray(view: View) {
        val inputText = binding.numberInputBox.text.toString()

        if (inputText == "") {
            displayCmds("กรุณาใส่เลขที่ต้องการเพิ่ม!")
            return
        }
        if (!inputText.isDigitsOnly() || inputText.length != 6) {
            displayCmds("กรุณาใส่ตัวเลข 6 หลักเท่านั้น!")
            return
        }

        adapter?.add(inputText)
        displayCmds("เพิ่มเลข $inputText แล้ว")
        binding.numberInputBox.getText().clear()
    }

    fun checkLottery(view: View) {
        if (numbersToCheck.isEmpty()) {
            displayCmds("ยังไม่มีเลขให้ตรวจ")
            return
        }

        displayCmds("กำลังตรวจ...")
        for (i in numbersToCheck.indices) {
            val number = numbersToCheck[i]
            val results = mutableListOf<String>()

            if (firstPrizeNumbers.contains(number)) results.add("ถูกรางวัลที่ 1")
            if (secondPrizeNumbers.contains(number)) results.add("ถูกรางวัลที่ 2")
            if (thirdPrizeNumbers.contains(number)) results.add("ถูกรางวัลที่ 3")
            if (fourthPrizeNumbers.contains(number)) results.add("ถูกรางวัลที่ 4")
            if (fifthPrizeNumbers.contains(number)) results.add("ถูกรางวัลที่ 5")

            // Special checks
            if (lastTwoPrizeNumbers.contains(number.takeLast(2))) results.add("ถูกเลขท้าย 2 ตัว")
            if (lastThreePrizeNumbers.contains(number.takeLast(3))) results.add("ถูกเลขท้าย 3 ตัว")
            if (firstThreePrizeNumbers.contains(number.take(3))) results.add("ถูกเลขหน้า 3 ตัว")
            if (nearFirstPrizeNumbers.contains(number)) results.add("ถูกรางวัลข้างเคียงรางวัลที่ 1")

            val resultText = if (results.isEmpty()) "ไม่ถูกรางวัล" else results.joinToString(", ")
            numbersToCheck[i] = "$number -> $resultText"
        }

        displayCmds("ตรวจเสร็จเรียบร้อย!")
        adapter?.notifyDataSetChanged()
    }

    fun clearTaskList(view: View) {
        numbersToCheck.clear()

        val preference = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.remove(TASK_LIST_PREF_KEY)
        editor.commit()

        adapter?.notifyDataSetChanged()
        displayCmds("ล้างข้อมูลสำเร็จ")
    }

    fun getMonthInThai(month: String): String {
        return when (month) {
            "01" -> "มกราคม"
            "02" -> "กุมภาพันธ์"
            "03" -> "มีนาคม"
            "04" -> "เมษายน"
            "05" -> "พฤษภาคม"
            "06" -> "มิถุนายน"
            "07" -> "กรกฎาคม"
            "08" -> "สิงหาคม"
            "09" -> "กันยายน"
            "10" -> "ตุลาคม"
            "11" -> "พฤศจิกายน"
            "12" -> "ธันวาคม"
            else -> "ไม่ทราบ"
        }
    }

    fun setDisplayDate(displayDate: DisplayDate?) {
        if (displayDate != null) {
            val monthInThai = getMonthInThai(displayDate.month)
            val formattedDate = "${displayDate.date.toInt().toString()} $monthInThai ${(displayDate.year.toInt() + 543).toString()}"
            binding.displayDateText.text = formattedDate
        } else {
            binding.displayDateText.text = "ข้อมูลวันที่ไม่พร้อมใช้งาน"
        }
    }

    fun fetchLotteryData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.glo.or.th/api/lottery/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(LotteryAPIService::class.java)
        val call = service.getLatestLottery()

        call.enqueue(object : Callback<LotteryResponse> {
            override fun onResponse(call: Call<LotteryResponse>, response: Response<LotteryResponse>) {
                if (response.isSuccessful) {
                    displayCmds("ดึงข้อมูลจากกองสลากสำเร็จ, พร้อมสำหรับการตรวจ")
                    val body = response.body()

                    firstPrizeNumbers = body?.response?.data?.first?.number?.map { it.value } ?: emptyList()
                    secondPrizeNumbers = body?.response?.data?.second?.number?.map { it.value } ?: emptyList()
                    thirdPrizeNumbers = body?.response?.data?.third?.number?.map { it.value } ?: emptyList()
                    fourthPrizeNumbers = body?.response?.data?.fourth?.number?.map { it.value } ?: emptyList()
                    fifthPrizeNumbers = body?.response?.data?.fifth?.number?.map { it.value } ?: emptyList()

                    lastTwoPrizeNumbers = body?.response?.data?.last2?.number?.map { it.value } ?: emptyList()
                    lastThreePrizeNumbers = body?.response?.data?.last3b?.number?.map { it.value } ?: emptyList()
                    firstThreePrizeNumbers = body?.response?.data?.last3f?.number?.map { it.value } ?: emptyList()
                    nearFirstPrizeNumbers = body?.response?.data?.near1?.number?.map { it.value } ?: emptyList()

                    println("First Prize: $firstPrizeNumbers")
                    println("Second Prize: $secondPrizeNumbers")
                    println("Third Prize: $thirdPrizeNumbers")
                    println("Fourth Prize : $fourthPrizeNumbers")
                    println("Fifth Prize: $fifthPrizeNumbers")
                    //--
                    println("Last 2 Prize: $lastTwoPrizeNumbers")
                    println("Last 3 Prize: $lastThreePrizeNumbers")
                    println("First 3 Prize: $firstThreePrizeNumbers")
                    println("Near 1 Prize: $nearFirstPrizeNumbers")

                    val displayDate = body?.response?.displayDate
                    setDisplayDate(displayDate)
                } else {
                    displayCmds("API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LotteryResponse>, t: Throwable) {
                displayCmds("Fetch API ล้มเหลว!")
                println("API Failure: ${t.message}")
            }
        })
    }
}
