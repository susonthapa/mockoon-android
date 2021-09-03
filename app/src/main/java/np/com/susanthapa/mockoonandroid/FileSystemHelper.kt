package np.com.susanthapa.mockoonandroid

import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/**
 * Created by suson on 8/31/21
 */
object FileSystemHelper {

    fun deleteFolderRecursively(file: File): Boolean {
        return try {
            var res = true
            file.listFiles()?.forEach {
                res = if (it.isDirectory) {
                    res and deleteFolderRecursively(it)
                } else {
                    res and it.delete()
                }
            }
            res
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun copyAssetFolder(manager: AssetManager, assetPath: String, toPath: String): Boolean {
        try {
            val files = manager.list(assetPath)!!
            var res = true;
            if (files.isEmpty()) {
                res = res and copyAsset(manager, assetPath, toPath)
            } else {
                File(toPath).mkdirs()
                files.forEach {
                    res = res and copyAssetFolder(manager, "${assetPath}/$it", "${toPath}/$it")
                }
            }
            return res
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun copyAsset(manager: AssetManager, assetPath: String, toPath: String): Boolean {
        return try {
            File(toPath).createNewFile()
            val inputStream = manager.open(assetPath)
            val outputStream = FileOutputStream(toPath)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false;
        }
    }

}