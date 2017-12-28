# Android Photo App [![Slack](https://slack.minio.io/slack?type=svg)](https://slack.minio.io) 

![minio_ANDROID1](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID1.jpg?raw=true)

本示例将会指导你如何构建一个简单的Android Photo app。在这个app中，你将会学习一个Android client是如何访问Photo API Service并随机加载一张图片。你可以通过[这里](https://github.com/minio/android-photo-app)获取完整的代码，代码是以Apache 2.0 License发布的。


## 1. 依赖

我们将使用Android Studio进行开发。这个app也会访问我们发布的Photo API Service来随机获取一张图片的presigned url。

* Android Studio 
* JDK 1.8

## 2. 设置  


 * 步骤1 - 启动Android Studio -> New Project -> Create a new Android Project。将你的工程命名为AndroidPhotoApp。


![minio_ANDROID2](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID2.jpg?raw=true)


 * 步骤2 - 选择Phone & Tablet。在本示例中，我们选择Marshmallow Android 6.0 SDK来编译和构建这个app。 
 
![minio_ANDROID3](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID3.png?raw=true)
 

 * 步骤3 - 选择一个Blank或者Basic Activity模板，然后点击Next。

![minio_ANDROID4](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID4.jpg?raw=true)


 * 步骤4 - 不用修改Activity Name和Layout Name,直接点击Finish。

![minio_ANDROID5](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID5.jpg?raw=true)

   
 * 步骤5 - 你应该可以看见gradle进行build并创建了一个工程，现在我们可以开始敲代码了。

![minio_ANDROID6](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID6.jpg?raw=true)


## 3. App Layout

删除初始的Hello World TextView。  

 * 拖拽一个Button widget到content_main.xml。
 * 从Layouts（在palette下面）里拖拽一个FrameLayout。
 * 然后从widgets中拖拽一个imageView到刚才的FrameLayout中。

![minio_ANDROID7](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID7.jpg?raw=true)

 
下面就是content_main.xml的完整xml。 

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="examples.minio.com.androidphotoapp.MainActivity"
    tools:showIn="@layout/activity_main">

    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Button"
        tools:layout_constraintTop_creator="1"
        tools:layout_constraintRight_creator="1"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_marginTop="16dp"
        tools:layout_constraintLeft_creator="1"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <FrameLayout
        android:layout_width="0dp"
        android:layout_height="308dp"
        tools:layout_constraintTop_creator="1"
        tools:layout_constraintRight_creator="1"
        android:layout_marginStart="36dp"
        android:layout_marginEnd="36dp"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/button"
        tools:layout_constraintLeft_creator="1"
        app:layout_constraintLeft_toLeftOf="parent">

        <ImageView
            android:id="@+id/imageView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:srcCompat="@android:color/background_light" />
    </FrameLayout>

</android.support.constraint.ConstraintLayout>
```

## 4. MainActivity.java 

我们将会用到之前构建的Phtoto API Service来给我们的AndroidPhotoApp提供服务。为了简单起见，我们没有用到ListView或者是GridView来显示图片列表，我们只是从PhotoAPI Service返回的多个presigned URL中随机选一个进行加载。

```java
package examples.minio.com.androidphotoapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button refreshButton;
    ProgressDialog pDialog;
    private static final String PHOTOSERVICE_URL = "http://play.minio.io:8080/PhotoAPIService-0.0.1-SNAPSHOT/minio/photoservice/list";
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //For the sake of simplicity we use an array of images. We recommend using ListViews or GridViews in your real applications.
        imageView = (ImageView) findViewById(R.id.imageView);

        refreshButton = (Button) findViewById(R.id.button);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An async task fetches all the latest photo URLs from the PhotoAPIService.
                new LoadImage().execute(PHOTOSERVICE_URL);
            }
        });
    }
```

我们会用一个AsyncTask在后台线程中来获取并处理这些url,而不是在UI线程中，这也是Android开发的惯用方式。

```java
private class LoadImage extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Fetching Image from Minio Server....");
            pDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            InputStream inputStream = null;
            String result = "";

            try {
                URL url = new URL(args[0]);
              
                HttpURLConnection httpCon =
                        (HttpURLConnection) url.openConnection();

                if (httpCon.getResponseCode() != 200)
                    throw new Exception("Failed to connect");
								
                // Fetch the content as an inputStream.
                inputStream = httpCon.getInputStream();

                // Convert the fetched inputstream to string.
                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

                if (result != null) System.out.println(result);

                // convert String to JSONObject.
                JSONObject json = new JSONObject(result);

                // get the array of photos.
                JSONArray imageJSON = json.getJSONArray("Album");
                int index = imageJSON.length()-1;

                Random rand = new Random();

                // Let's get a randomly pic a picture to load.
                int rindex = rand.nextInt((index - 0) + 1) + 0;
                 

                // Return the image.
                return BitmapFactory.decodeStream(new URL(imageJSON.getJSONObject(rindex).getString("url")).openStream());
            } catch (Exception e) {

                System.out.println(e.getMessage());
            }
            return null;

        }


        protected void onPostExecute(Bitmap image) {
            System.out.println("In Post Execute");
            if (image != null) {
                pDialog.dismiss();
                // Place the image on the ImageView.
                imageView.setImageBitmap(image);

            } else
            {
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }

        }

    }
```

我们用到一个简单的工具方法将InputStream转成String。

```java
private static String convertInputStreamToString(InputStream inputStream) throws IOException{
  
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
  
  	    // Loop through the stream line by line and convert to a String.
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
```

## 5. AndroidManifest.xml

我们需要在AndroidManifest.xml中加上`<uses-permission android:name="android.permission.INTERNET" />`，这样app才可以通过网络获取图片。

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="examples.minio.com.androidphotoapp">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:label="@string/app_name"
            android:theme="@style/AppTheme.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

## 6. 运行App

* 启动一个Android模拟器或者连接一个Android设备到你的电脑。
* 请确认你的模拟器/设备的Android版本是6.0+。
* 点击这个绿色的`play`按钮部署并运行你的app。
* 点击`Load Random Image Button`随机加载一张图片。

![minio_ANDROID8](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID8.jpg?raw=true)


## 7. 了解更多

- [Photo API Service Example](https://docs.minio.io/docs/java-photo-api-service)
- [Using `minio-java` client SDK with Minio Server](https://docs.minio.io/docs/java-client-quickstart-guide) 
- [Minio Java Client SDK API Reference](https://docs.minio.io/docs/java-client-api-reference)

