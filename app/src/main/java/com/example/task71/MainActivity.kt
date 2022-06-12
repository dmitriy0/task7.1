package com.example.task71

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private val fileName = "json.txt"
    private lateinit var swipeRefresh:SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            requestToApi()
            Toast.makeText(this@MainActivity, "fromApi", Toast.LENGTH_LONG).show()
            //todo add diffutil
        }

        val path = this@MainActivity.filesDir.absolutePath + "/" + fileName
        val file = File(path)

        if (file.exists()){

            val fileInputStream: FileInputStream = this@MainActivity.openFileInput(fileName)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            recyclerView.adapter = Adapter(jsonToHero(stringBuilder.toString()), this@MainActivity)
            Toast.makeText(this@MainActivity, "fromFile", Toast.LENGTH_LONG).show()

        } else {

            requestToApi()
            Toast.makeText(this@MainActivity, "fromApi", Toast.LENGTH_LONG).show()
        }

    }

    private fun requestToApi(){

        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url("https://api.opendota.com/api/herostats").build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
            }
            override fun onResponse(call: Call, response: Response) {
                val stringResponse = response.body!!.string()
                this@MainActivity.runOnUiThread{

                    val filename = "json.txt"
                    val outputStream: FileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE)
                    outputStream.write(stringResponse.toByteArray())
                    outputStream.close()

                    recyclerView.adapter = Adapter(jsonToHero(stringResponse), this@MainActivity)
                    
                    swipeRefresh.isRefreshing = false
                }
            }

        })
    }

    private fun jsonToHero(json: String): ArrayList<Hero> {

        val type = Types.newParameterizedType(List::class.java, Hero::class.java)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter<ArrayList<Hero>>(type)
        return adapter.fromJson(json)!!

    }

}
