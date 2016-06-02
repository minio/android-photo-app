/*
 * AndroidPhotoApp Example, (C) 2016 Minio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.minio.com.myphotosapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

        //For the sake of simplicity we use an array of images. We recommend using ListViews or GridViews in your real applications
        imageView = (ImageView) findViewById(R.id.imageView);


        refreshButton = (Button) findViewById(R.id.button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadImage().execute(PHOTOSERVICE_URL);
            }
        });

    }


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

                inputStream = httpCon.getInputStream();

                // convert inputstream to string
                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

                if (result != null) System.out.println(result);

                // convert String to JSONObject
                JSONObject json = new JSONObject(result);

                // get the array of photos
                JSONArray imageJSON = json.getJSONArray("Album");
                int index = imageJSON.length()-1;

                Random rand = new Random();

                // lets get a randomly pic a picture to load
                int rindex = rand.nextInt((index - 0) + 1) + 0;
                System.out.println(rindex);

                //return image;
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
                imageView.setImageBitmap(image);

            } else

            {
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }

        }

    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
