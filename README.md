# mockoon-android
Mockoon for Android

This is the android port of mocking server [Mockoon](https://github.com/mockoon/mockoon). Export your mockoon environment and then run it in android.
![image](https://user-images.githubusercontent.com/33973551/134814817-df548b5e-7bda-4e95-838a-737d039be701.png)

### Demo
![mockoon-demo](https://user-images.githubusercontent.com/33973551/134814853-6596ce81-127e-41f0-9614-472bca27bd7e.gif)

### Usage
This library will add around **60MB** to your app, so you might want to create a dedicated variant.

#### Create mock build variant
```groovy
        mock {
            initWith debug
            applicationIdSuffix ".mock"
        }
```

#### Add the depedency
Add this dependency in your app layer's `build.gradle`.
```groovy
    mockImplementation 'np.com.susanthapa:mockoon-android:0.6.1'
```

#### Start Mockoon
Use reflection to start mockoon as gradle won't be able to resolve the dependency for all variants. Create a helper function to start the server like this.
```kotlin
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
```
The `start` function accepts two arguments, atleast one argument is required. The first argument `mockPath` can be used if you have location of the mock file.
This is mostly useful when you want to bundle the environment with your app as assets. You can use the second argument `uri` if you want to load the environment
from file systems.

