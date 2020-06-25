package com.example.progetto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

public class ProfileActivity extends AppCompatActivity {

    public String session_id, username, imgStr, lp, xp;
    TextView tv_username, tv_lifePoints, tv_experiencePoints;
    TextView et_username;
    ImageView image;
    SharedPreferences firstRun, sharedUser, sharedImg, sharedLP, sharedXP;
    public final String URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstRun = getPreferences(Context.MODE_PRIVATE);

        // SharedPreferences per dati profilo
        sharedUser = getPreferences(Context.MODE_PRIVATE);
        sharedImg = getPreferences(Context.MODE_PRIVATE);
        sharedLP = getPreferences(Context.MODE_PRIVATE);
        sharedXP = getPreferences(Context.MODE_PRIVATE);

        session_id = getIntent().getStringExtra("session_id");
        //Log.d("SessionID", session_id);

        if(checkFirstRun(firstRun)) {
            setContentView(R.layout.activity_setprofile);
            et_username = findViewById(R.id.textView);
            image = findViewById(R.id.imageView4);
            Log.d("Profile","Set");

        }
        else {
            setContentView(R.layout.activity_getprofile);
            tv_username = findViewById(R.id.tv_username);
            image = findViewById(R.id.imageView4);
            tv_lifePoints = findViewById(R.id.tv_lifePoints);
            tv_experiencePoints = findViewById(R.id.tv_experiencePoints);
            Log.d("Profile","Get");
            getProfileInfo();
        }

    }

    public static boolean usernameIsOk(String str) {
        if(str.length() > 0 && str.length() <= 15)
            return true;
        return false;
    }

    public static boolean imgIsOk(String str) {
        if(str != null && !str.isEmpty())
            return true;
        return false;
    }

    public void uploadImg(View v){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    public void noConnection() {
        Toast.makeText(ProfileActivity.this, R.string.errorNoConnection, Toast.LENGTH_LONG).show();
    }

    public void onClickPlay(View v) {
        username = et_username.getText().toString();

        Bitmap b = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.avatar);
        String imgStrDefault = encodeToBase64(b);

        Log.d("Register", "user: "+username+" img: "+imgStr);

        if(usernameIsOk(username) || imgIsOk(imgStr))
        {
            new setProfileAsyncTask().execute();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            View customLayout = getLayoutInflater().inflate(R.layout.dialog_profile_error, null);
            builder.setView(customLayout);

            ImageButton btnChiudi = customLayout.findViewById(R.id.btn_dialogChiudi);
            final AlertDialog dialog = builder.create();
            btnChiudi.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    private boolean checkFirstRun(SharedPreferences sharedPref) {
        if (sharedPref.getBoolean("my_first_time", true)) {
            Log.d("FirstTime", "First time");
            sharedPref.edit().putBoolean("my_first_time", false).commit();
            return true;
        }
        else
            Log.d("FirstTime", "Not First time");
        return false;
    }

    public void backToMainActivity(View v) {
        Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(backToMain);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode)
            {
                case 1:
                    if (resultCode == RESULT_OK) {
                        Uri imgUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);

                        if(encodeToBase64(bitmap).length() <= 137000) {
                            image.setImageBitmap(bitmap);
                            imgStr = encodeToBase64(bitmap);
                            Log.d("Image", imgStr);
                        }
                        else
                            Toast.makeText(ProfileActivity.this, "Immagine non supportata", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("session_id", session_id);
        bundle.putString("username", username);
        bundle.putString("imgStr", imgStr);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void changeData(View v) {
        addFragment(new FragmentProfile(), false, "fragmentProfile");
    }

    public String encodeToBase64(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encImage;
    }

    public Bitmap decodeFromBase64(String s) {
        byte[] imageBytes = Base64.decode(s, Base64.DEFAULT);
        Bitmap decodeImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodeImage;
    }

    public void getProfileInfo() {
        final JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", session_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(
                URL + "getprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response",response.toString());
                        try {
                            String user = response.getString("username");
                            String img = response.getString("img");
                            String lp = response.getString("lp");
                            String xp = response.getString("xp");

                            Log.d("Image", "img: "+img);

                            sharedLP.edit().putString("sharedLP", lp).commit();
                            sharedXP.edit().putString("sharedXP", xp).commit();

                            username = user;
                            imgStr = img;

                            if(!user.equals("null"))
                                tv_username.setText(user);
                            else
                                tv_username.setText("");

                            if(!img.equals("null"))
                                image.setImageBitmap(decodeFromBase64(img));

                            tv_lifePoints.setText(lp);
                            tv_experiencePoints.setText(xp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "Error: " + error.toString());
                noConnection();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(ProfileActivity.this);
        queue.add(request);
    }

    private class setProfileAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            final JSONObject jsonBody = new JSONObject();

            //Riempio jsonBody
            try {
                jsonBody.put("session_id", session_id);
                jsonBody.put("username", username);
                jsonBody.put("img", imgStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("jsonBody",jsonBody.toString());

            final JsonObjectRequest request = new JsonObjectRequest(
                    URL + "setprofile.php",
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            Log.d("jsonSet", response.toString());
                            /*
                            sharedUser.edit().putString("sharedUser", username).commit();
                            sharedImg.edit().putString("sharedImg", imgStr).commit();*/
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley", "Error: " + error.toString());
                    noConnection();
                }
            });
            RequestQueue queue = Volley.newRequestQueue(ProfileActivity.this);
            queue.add(request);

            return null;
        }
    }
}
