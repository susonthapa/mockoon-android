package np.com.susanthapa.mockoonandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import np.com.susanthapa.mockoonandroid.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                startMock(uri = it.data?.data)
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

    private fun startMock(mockPath: String? = null, uri: Uri? = null) {
        if (BuildConfig.BUILD_TYPE == "mock") {
            // use reflection as we don't want to include this in other build variants
            try {
                val mockClass = Class.forName("np.com.susanthapa.mockoon_android.MockoonAndroid")
                val constructor = mockClass.getConstructor(String::class.java, Uri::class.java)
                val mock = constructor.newInstance(mockPath, uri)
                val startMethod = mockClass.getMethod("startMock", Context::class.java)
                startMethod.invoke(mock, this)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}