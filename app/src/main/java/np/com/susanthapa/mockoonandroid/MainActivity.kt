package np.com.susanthapa.mockoonandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import np.com.susanthapa.mockoonandroid.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val tag = "Mockoon"

    // load native-lib and node
    companion object {
        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("node")
        }

        private var isNodeStared = false
        const val PREF_NAME = "NODEJS_MOBILE_PREFS"
        const val APK_UPDATE_KEY = "nodeApkLastUpdated"

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

        if (!isNodeStared) {
            isNodeStared = true
            Thread {
                Log.d(tag, "onCreate: node started")
                val nodeDir = "${applicationContext.externalCacheDir?.absolutePath}/nodejs-project"
                val nodeFile = File(nodeDir)
                if (!nodeFile.exists()) {
//                    if (nodeFile.exists()) {
//                        FileSystemHelper.deleteFolderRecursively(nodeFile)
//                    }
                    // copy the files to the files system
                    FileSystemHelper.copyAssetFolder(
                        applicationContext.assets,
                        "nodejs-project",
                        nodeDir
                    )
                    Log.d(
                        tag, "onCreate: copied files" +
                                ""
                    )
                    saveLastUpdateTime()
                }
                // parse json file
                val allEnv = File("${nodeDir}/server.json").inputStream()
                    .bufferedReader().use { it.readText() }
                val jsonEnv = JSONObject(allEnv).getJSONArray("data")
                // loop through all environments and start it in separate threads
                for (i in 0 until jsonEnv.length()) {
                    val env = jsonEnv.getJSONObject(i).getJSONObject("item")
                    Log.d(tag, "starting environment: ${env.getString("name")}")
                    Log.d(tag, "starting environment: $env")
                    Thread {
                        startNodeWithArguments(
                            arrayOf(
                                "node",
                                "${nodeDir}/main.js",
                                env.toString(),
                            )
                        )
                    }.start()
                }
            }.start()
        }
    }

    /**
     * A native method that is implemented by the 'mockoonandroid' native library,
     * which is packaged with this application.
     */
    private external fun startNodeWithArguments(arguments: Array<String>): Int
}