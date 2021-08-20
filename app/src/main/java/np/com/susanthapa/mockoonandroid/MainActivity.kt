package np.com.susanthapa.mockoonandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import np.com.susanthapa.mockoonandroid.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // load native-lib and node
    companion object {
        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("node")
        }

        private var isNodeStared = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isNodeStared) {
            isNodeStared = true
            Thread {
                startNodeWithArguments(
                    arrayOf(
                        "node", "-e",
                        """
                            var http = require('http');
                            var versions_server = http.createServer( (request, response) => {
                                response.end('Versions: ' + JSON.stringify(process.versions));
                            });
                            versions_server.listen(3000);
                        """.trimIndent()
                    )
                )
            }.start()
        }

        var response = ""
        val handler = Handler(Looper.getMainLooper()) {
            binding.sampleText.text = response
            return@Handler true
        }
        binding.sendRequest.setOnClickListener {
            Thread {
                try {
                    val server = URL("http://localhost:3000/")
                    val reader = BufferedReader(InputStreamReader(server.openStream()))
                    val inputLine = reader.readLines().fold("") { acc, current ->
                        acc + current
                    }
                    reader.close()
                    response = inputLine
                    handler.sendEmptyMessage(0)
                } catch (e: Exception) {
                    e.printStackTrace()
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