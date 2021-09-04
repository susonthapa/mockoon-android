package np.com.susanthapa.mockoonandroid

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import np.com.susanthapa.mockoon_android.MockoonAndroid
import np.com.susanthapa.mockoonandroid.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // load native-lib and node
    companion object {
        const val PREF_NAME = "NODEJS_MOBILE_PREFS"
        const val APK_UPDATE_KEY = "nodeApkLastUpdated"

    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val mockoon =
                    MockoonAndroid(uri = it.data?.data)
                mockoon.startMock(this)
            }
        }


    private fun wasAPKUpdated(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val prevUpdateTime = prefs.getLong(APK_UPDATE_KEY, 0)
        val packageInfo =
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        val lastUpdated = packageInfo.lastUpdateTime

        return lastUpdated != prevUpdateTime
    }

    private fun saveLastUpdateTime() {
        val packageInfo =
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        val lastUpdated = packageInfo.lastUpdateTime
        val prefs = applicationContext.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putLong(APK_UPDATE_KEY, lastUpdated)
            apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendRequest.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            resultLauncher.launch(intent)
        }
    }


}