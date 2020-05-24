package com.example.instafox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity {
    public static int REQUEST_EXTERNAL_STORAGE = 1;
    private RequestQueue mQueue;
    private SearchView searchView;
    private ImageView profilePicture;
    private Button downloadButton;
    private String url, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SweetAlertDialog(this)
                .setTitleText("Welcome on InstaFox !")
                .setContentText("Type the username of the person you want in Instagram to see his profile picture in large size with the possibility of download !")
                .show();
        url = "https://instagram.frba3-1.fna.fbcdn.net/v/t51.2885-19/s320x320/72114074_994859947556229_1533699290591920128_n.jpg?_nc_ht=instagram.frba3-1.fna.fbcdn.net&_nc_ohc=-f9AdMMMo-QAX9hKKOM&oh=d37408ecffe57a24f1bc038648c7e586&oe=5EF2D464";
        username = "loutfialiluch";
        profilePicture = findViewById(R.id.imgProfilePic);
        downloadButton = findViewById(R.id.downloadButton);
        searchView = findViewById(R.id.mySearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                username = query.trim().replaceAll("\\s", "");
                url = "https://www.instagram.com/" + username + "/?__a=1";
                mQueue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject1 = response.getJSONObject("graphql");
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("user");
                            Picasso.get().load(jsonObject2.getString("profile_pic_url_hd")).into(profilePicture);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                mQueue.add(request);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        });


    }

    public void saveImageIntoGallery(String url, String username)
    {
        String filename = username+".jpg";
        String DIR_NAME = "InstaFoxImages";
        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + DIR_NAME + "/");


        if (!direct.exists()) {
            direct.mkdir();
        }

        DownloadManager dm = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                        File.separator + DIR_NAME + File.separator + filename);

        dm.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_EXTERNAL_STORAGE)
        {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                saveImageIntoGallery(url, username);
            else
            {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
