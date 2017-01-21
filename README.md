# Android Photo App [![Slack](https://slack.minio.io/slack?type=svg)](https://slack.minio.io) 

![minio_ANDROID1](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID1.jpg?raw=true)

This example will guide you through the code to build a simple Android Photo app. In this app, you will learn how an Android client can use the Photo API Service and load a random image. Full code is available here : https://github.com/minio/android-photo-app, released under Apache 2.0 License.


## 1. Dependencies

We will be building this app using Android Studio. This app will also consume the Photo API Service we built to get presigned urls that are randomly loaded on click of a button.

* Android Studio 
* JDK 1.8

## 2. SetUp  


 * Step 1 - Launch Android Studio -> New Project -> Create a new Android Project. Name your project AndroidPhotoApp.


![minio_ANDROID2](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID2.jpg?raw=true)


 * Step 2 - Select Phone & Tablet. In this example we choose the latest stable Marshmallow Android 6.0 SDK to compile and build this app. 
 
![minio_ANDROID3](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID3.jpg?raw=true)
 

 * Step 3 - Pick a Blank or a Basic Activity template and then click on Next.

![minio_ANDROID4](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID4.jpg?raw=true)


 * Step 4 - Leave the defaults as is for the Activity & Layout Names. Click Finish.

![minio_ANDROID5](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID5.jpg?raw=true)

   
 * Step 5 - You should see that gradle builds and generates a project where we can now begin to code up our AndroidPhotoApp.

![minio_ANDROID6](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID6.jpg?raw=true)


## 3. App Layout

Next let's remove the default Hello World TextView.  

 * Drag and drop a Button widget onto the phone's screen presented on content_main.xml.
 * Drag and drop a FrameLayout from the Design Palette below the button. 
 * Let's also drag and drop an imageView from the widgets inside the FrameLayout.

![minio_ANDROID7](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID7.jpg?raw=true)

 
Below is also the full XML version of our design from content_main.xml. 

```xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="example.aau.com.myphotosapp.MainActivity"
    tools:showIn="@layout/activity_main"
    android:background="#585454">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Load Random Image"
        android:id="@+id/button"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true" />

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_alignParentStart="true"
        android:layout_below="@+id/button">

        <ImageView
            android:layout_width="364dp"
            android:layout_height="369dp"
            android:id="@+id/imageView"
            android:layout_gravity="left|top" />

    </FrameLayout>

</RelativeLayout>

```

## 4. MainActivity.java 

We will use the Photo API Service we built earlier to service our AndroidPhotoApp. For the sake of simplicity, we will not use a ListView or a GridView to display a collection of photos. Instead we will randomly load one of the photos from the presigned URLs we receive from the PhotoAPI Service.

```java

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
        
        // Set up and Onclick Listener for the load random images button.
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              
                // An async task fetches all the latest photo URLs from the PhotoAPIService.
                new LoadImage().execute(PHOTOSERVICE_URL);
            }
        });


    }
```

As is customary in fetching content for Android clients, we will employ an AsyncTask to fetch and process the urls in the background thread off from the main UI thread. 

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

We use a small helper function to convert the InputStream to String.

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

We need to add the <uses-permission android:name="android.permission.INTERNET" /> so that the app can fetch the images over the internet.

```xml

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="example.aau.com.myphotosapp">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
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

## 6. Run the App

* Launch the Android Emulator or connect an Android device to your computer. 
* Please ensure the emulator/device runs Android 6.0 Marshmallow or above so that we can deploy our app to this emulator. This is because when we created this project, we picked Android 6.0 for the minimum SDK settings. See step 2 in the Setup section.
* Press the green play button to run & deploy the app onto the emulator or a connected Android device. 
* Click on the Load Random Image Button to load a different image.

![minio_ANDROID8](https://github.com/minio/android-photo-app/blob/master/docs/screenshots/minio-ANDROID8.jpg?raw=true)


## 7. Explore Further

- [Photo API Service Example](https://docs.minio.io/docs/java-photo-api-service)
- [Using `minio-java` client SDK with Minio Server](https://docs.minio.io/docs/java-client-quickstart-guide) 
- [Minio Java Client SDK API Reference](https://docs.minio.io/docs/java-client-api-reference)

