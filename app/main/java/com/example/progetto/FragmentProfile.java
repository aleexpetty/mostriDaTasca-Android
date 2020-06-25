package com.example.progetto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class FragmentProfile extends Fragment {
    public String session_id, username, imgStr;
    TextView tv_username;
    ImageView iv_img;
    ImageButton closeBtn;
    Button saveBtn;
    public final String URL = "https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        tv_username = v.findViewById(R.id.fragmentUsername);
        iv_img = v.findViewById(R.id.fragmentImg);
        closeBtn = v.findViewById(R.id.btn_closeFragment);
        saveBtn = v.findViewById(R.id.btn_saveFragment);
        closeBtn.setOnClickListener(buttonListener);
        saveBtn.setOnClickListener(buttonListener);
        iv_img.setOnClickListener(buttonListener);

        session_id = getArguments().getString("session_id");
        username = getArguments().getString("username");
        imgStr = getArguments().getString("imgStr");

        Log.d("FragmentProfile", "username: "+username+" img: "+imgStr);

        if(!username.equals("null"))
            tv_username.setText(username);

        if(!imgStr.equals("null"))
            iv_img.setImageBitmap(decodeFromBase64(imgStr));
        return v;
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_closeFragment:
                    Fragment f = getFragmentManager().findFragmentByTag("fragmentProfile");
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.remove(f).commit();
                    break;

                case R.id.fragmentImg:
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 1);
                    break;

                case R.id.btn_saveFragment:
                    setProfile();
                    break;

            }

        }
    };

    public void setProfile(){

        username = tv_username.getText().toString();
        Log.d("Username", "Click "+username);

        if(usernameIsOk(username) || imgIsOk(imgStr)) {
            final JSONObject jsonBody = new JSONObject();
            Log.d("Username", "IF "+username);
            Log.d("Username", "IF "+imgStr);

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
                    URL,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("jsonSet", response.toString());
                            Fragment f = getFragmentManager().findFragmentByTag("fragmentProfile");
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.remove(f).commit();
                            ((ProfileActivity)getActivity()).getProfileInfo();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley", "Error: " + error.toString());
                    Toast.makeText(getContext(), R.string.errorNoConnection, Toast.LENGTH_LONG).show();
                }
            });
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(request);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode)
            {
                case 1:
                    if (resultCode == RESULT_OK) {
                        Log.d("Image","Set new image");
                        Uri imgUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imgUri);

                        if(encodeToBase64(bitmap).length() <= 137000) {
                            iv_img.setImageBitmap(bitmap);
                            imgStr = encodeToBase64(bitmap);
                            Log.d("NewImage", imgStr);
                        }
                        else
                            Toast.makeText(getContext(), "Immagine non supportata", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
