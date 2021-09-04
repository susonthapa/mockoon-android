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

        const val tag = "MockoonAndroid"
        var hasStarted = false
    }

    fun startMock(context: Context) {
        if (!hasStarted) {
            hasStarted = true
            Thread {
                Log.d(tag, "onCreate: node started")
                val nodeDir = "${context.externalCacheDir?.absolutePath}/nodejs-project"
                val nodeFile = File(nodeDir)
                if (!nodeFile.exists()) {
//                    if (nodeFile.exists()) {
//                        FileSystemHelper.deleteFolderRecursively(nodeFile)
//                    }
                    // copy the files to the files system
                    np.com.susanthapa.mockoon_android.FileSystemHelper.copyAssetFolder(
                        context.assets,
                        "nodejs-project",
                        nodeDir
                    )
                    Log.d(
                        tag, "onCreate: copied files" +
                                ""
                    )
//                    saveLastUpdateTime()
                }

                // copy the file to our location
                val path = "$nodeDir/server.json"
                val mockName = if (mockPath != null) {
                    // copy file to our project directory
                    FileSystemHelper.copyAsset(context.assets, mockPath, path)
                    mockPath
                } else if (uri != null) {
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
                    path
                } else {
                    Log.w(tag, "please provide file from asset or uri for mockoon environment")
                    return@Thread
                }

                // start mockoon
                startNodeWithArguments(
                    arrayOf(
                        "node",
                        "${nodeDir}/main.js",
                        path,
                    )
                );
            }.start()

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