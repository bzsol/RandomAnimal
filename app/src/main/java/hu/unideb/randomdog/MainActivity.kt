package hu.unideb.randomdog

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import hu.unideb.randomdog.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@DelicateCoroutinesApi
class MainActivity : AppCompatActivity(),SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var isDog: String = "ND"
    private val DOGURL = "https://random.dog/"
    private val CATURL = "https://api.thecatapi.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // No dark mode activated
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setUpTabBar()
    }

    private fun setUpTabBar() {
        binding.bottomNav.setOnItemSelectedListener{
           when(it){
               R.id.nav_dog -> {
                   isDog = "Y"
                   binding.imageViewCat.visibility = View.GONE
                   setUpSensor()
               }
               R.id.nav_cat -> {
                   isDog = "N"
                   binding.imageViewDog.visibility = View.GONE
                   setUpSensor()
               }
           }
        }
    }

    private fun setUpSensor(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this,it,SensorManager.SENSOR_DELAY_FASTEST,SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onResume() {
        setUpSensor()
        super.onResume()
    }



    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val sides = p0.values[0]
            val upDown = p0.values[1]
            if((upDown >= 10 || sides >= 10)){
                sensorManager.unregisterListener(this)
                if(isDog == "Y"){
                    getPictureApi(DOGURL,isDog)
                }
                else if(isDog == "N"){
                    getPictureApi(CATURL,isDog)
                }
            }
        }
        setUpSensor()
    }
    @DelicateCoroutinesApi
    private fun getPictureApi(baseurl: String, bool: String) {
        lateinit var url: String
        val api = Retrofit.Builder().baseUrl(baseurl)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(Request::class.java)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if(bool == "Y"){
                    do {
                        url = api.getRandomDog().url
                    }while(!url.contains(".jpg"))
                }
                else if(bool == "N"){
                    url = api.getRandomCat()[0].url
                }
                withContext(Dispatchers.Main){
                   if(bool == "Y"){
                       Glide.with(applicationContext).load(url).into(binding.imageViewDog)
                       binding.imageViewDog.visibility = View.VISIBLE
                   }
                    else if(bool == "N"){
                       Glide.with(applicationContext).load(url).into(binding.imageViewCat)
                       binding.imageViewCat.visibility = View.VISIBLE
                    }
                }
            }
            catch (e: Exception){
                println("Image load failed")
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        println("changed")
    }


}


