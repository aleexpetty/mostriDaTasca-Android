package com.example.progetto;


import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;


public class FragmentMonster extends Fragment {
    TextView monsterName, monsterSize, monsterDamage, errorText;
    ImageView monsterImg;
    public String sID, targetID, monsterImgStr, size, name;
    public Double distance;
    public LatLng myPos, markerPos;
    public final String URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    Button btnFight;
    ImageButton btnClose;

    public FragmentMonster() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        targetID = getArguments().getString("id");
        sID = getArguments().getString("sID");
        size = getArguments().getString("size");
        name = getArguments().getString("name");
        myPos = getArguments().getParcelable("myPos");
        markerPos = getArguments().getParcelable("markerPos");

        Double d = myPos.distanceTo(markerPos);
        distance = Math.round(d * 100.0) / 100.0;
        Log.d("Distance", distance+"");

        View v;

        if(distance <= 50.0) {
            v = inflater.inflate(R.layout.fragment_monster, container, false);
            btnFight = v.findViewById(R.id.btn_fight);
            btnFight.setOnClickListener(buttonListener);
        }
        else {
            v = inflater.inflate(R.layout.fragment_monster_error, container, false);
            errorText = v.findViewById(R.id.tv_errorText);
        }

        monsterName = v.findViewById(R.id.tv_monsterName);
        monsterSize = v.findViewById(R.id.tv_monsterSize);
        monsterDamage = v.findViewById(R.id.tv_monsterDamage);
        monsterImg = v.findViewById(R.id.iv_monsterImg);
        btnClose = v.findViewById(R.id.ib_btnClose);
        btnClose.setOnClickListener(buttonListener);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        String dim="";
        String danno="";

        switch (size)
        {
            case "S":
                dim = "Piccolo";    danno = "0 - 50";   break;
            case "M":
                dim = "Medio";    danno = "25 - 75";    break;
            case "L":
                dim = "Grande";   danno = "50 - 100";    break;
        }

        setMonsterImg();
        monsterName.setText(name);
        monsterSize.setText(dim);
        monsterDamage.setText(danno);

        if(distance > 50.0)
        {
            int distanceInt = (int)Math.round(distance);
            errorText.setText("Distanza massima: 50m.\nDistanza attuale: "+distanceInt+"m.");
        }
    }

    public void setMonsterImg() {
        final JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", sID);
            jsonBody.put("target_id", targetID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(
                URL + "getimage.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            monsterImgStr = response.getString("img");
                            monsterImg.setImageBitmap(decodeFromBase64(monsterImgStr));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    public Bitmap decodeFromBase64(String s) {
        byte[] imageBytes = Base64.decode(s, Base64.DEFAULT);
        Bitmap decodeImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodeImage;
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_fight:

                    Log.d("Position", "Mia posizione "+myPos.toString());
                    Log.d("Position", "Marker posizione "+markerPos.toString());

                    if(distance <= 50.0) {

                        final JSONObject jsonBody = new JSONObject();

                        try {
                            jsonBody.put("session_id", sID);
                            jsonBody.put("target_id", targetID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        final JsonObjectRequest request = new JsonObjectRequest(
                                URL + "fighteat.php",
                                jsonBody,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("ResponseFightEat", response.toString());

                                        try {

                                            boolean died = response.getBoolean("died");
                                            String lp = response.getString("lp");
                                            String xp = response.getString("xp");

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            // set the custom layout
                                            final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                                            builder.setView(customLayout);

                                            TextView dialogLp = customLayout.findViewById(R.id.tv_dialogLp);
                                            TextView dialogXp = customLayout.findViewById(R.id.tv_dialogXp);
                                            ImageView imgDialog = customLayout.findViewById(R.id.imageView14);
                                            ImageButton btnChiudi = customLayout.findViewById(R.id.btn_dialogChiudi);

                                            if(died)
                                            {
                                                imgDialog.setImageResource(R.drawable.morte);
                                            }
                                            else {
                                                imgDialog.setImageResource(R.drawable.vittoria);
                                            }


                                            dialogLp.setText(lp);
                                            dialogXp.setText(xp);

                                            // create and show the alert dialog
                                            final AlertDialog dialog = builder.create();
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                            btnChiudi.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    dialog.dismiss();
                                                    Fragment f = getFragmentManager().findFragmentByTag("fragmentMonster");
                                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                    transaction.remove(f).commit();
                                                    ((MainActivity)getActivity()).getProfileInfo();
                                                    ((MainActivity)getActivity()).getMapObjects();
                                                }
                                            });

                                            dialog.show();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Volley", "Error: " + error.toString());
                            }
                        });
                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        queue.add(request);
                    }

                    break;

                case R.id.ib_btnClose:
                    Fragment f = getFragmentManager().findFragmentByTag("fragmentMonster");
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.remove(f).commit();
                    break;
            }
        }
    };


}
