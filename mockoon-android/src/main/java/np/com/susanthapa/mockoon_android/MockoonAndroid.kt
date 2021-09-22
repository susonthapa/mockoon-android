package np.com.susanthapa.mockoon_android

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class MockoonAndroid constructor(
    private val mockPath: String? = null,
    private val uri: Uri? = null
) {

    companion object {
        // Used to load the 'mockoon_android' library on application startup.
        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("node")
        }

        const val tag = "Mockoon-Android"
        var hasStarted = false
    }

    fun startMock(context: Context) {
        if (!hasStarted) {
            hasStarted = true
            Thread {
                val nodeDir = "${context.externalCacheDir?.absolutePath}/mockoon-android"
                val nodeFile = File(nodeDir)
                if (!nodeFile.exists()) {
                    // copy the files to the files system
                    FileSystemHelper.copyAssetFolder(
                        context.assets,
                        "mockoon-android",
                        nodeDir
                    )
                    Log.d(tag, "onCreate: copied files")
                }

                // copy the file to our location
                val path = "$nodeDir/server.json"
                when {
                    mockPath != null -> {
                        // copy file to our project directory
                        FileSystemHelper.copyAsset(context.assets, mockPath, path)
                    }
                    uri != null -> {
                        // get from uri
                        val outputStream = FileOutputStream(path)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            Log.w(tag, "failed to open provided uri")
                            return@Thread
                        }
                        inputStream.copyTo(outputStream)
                        outputStream.close()
                        outputStream.flush()
                        inputStream.close()
                    }
                    else -> {
                        Log.w(tag, "please provide file from asset or uri for mockoon environment")
                        return@Thread
                    }
                }

                // start mockoon
                startNodeWithArguments(
                    arrayOf(
                        "node",
                        "${nodeDir}/main.js",
                        path,
                    )
                )
                Log.d(tag, "onCreate: node started")
            }.apply {
                isDaemon = true
                start()
            }
        } else {
            Log.w(tag, "mockoon already started")
        }
    }

    /**
     * A native method that is implemented by the 'mockoon_android' native library,
     * which is packaged with this application.
     */
    external fun startNodeWithArguments(arguments: Array<String>): Int
}