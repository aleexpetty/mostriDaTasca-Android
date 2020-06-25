package com.example.progetto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class RankingActivity extends AppCompatActivity {

    private String sID;
    TextView username, lp, xp;
    ImageView img;
    RankingAdapter adapter;
    public final String URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        sID = getIntent().getStringExtra("session_id");
        Log.d("sID", sID);

        username = findViewById(R.id.username);
        img = findViewById(R.id.player_image);
        lp = findViewById(R.id.lifepoints);
        xp = findViewById(R.id.experience);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //CHIAMATA DI RETE PER PRENDERE IL RANK
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", sID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL + "ranking.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray array = response.getJSONArray("ranking");

                            PlayerModel.getInstance().removeAll();

                            for (int i = 0; i < array.length(); i++) {
                                Log.d("Volley","Correct: "+ array.getJSONObject(i).toString());

                                String user = array.getJSONObject(i).getString("username");
                                String im = array.getJSONObject(i).getString("img");
                                if(im.equals("null") || im.length() > 137000 || im.length() < 20){
                                    Bitmap b = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.avatar);
                                    im = encodeToBase64(b);
                                }
                                String xps = array.getJSONObject(i).getString("xp");
                                String lps = array.getJSONObject(i).getString("lp");
                                Player x = new Player(user, im, lps, xps);
                                PlayerModel.getInstance().addPlayer(x);
                                adapter.notifyDataSetChanged();

                                Log.d("Player","Player: "+x.getPlayer());
                            }

                            Log.d("PlayerModel","size: "+PlayerModel.getInstance().getSize());

                        }   catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Volley","Correct: "+ response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley","Error: "+error.toString());
                        noConnection();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void noConnection() {
        Toast.makeText(RankingActivity.this, R.string.errorNoConnection, Toast.LENGTH_LONG).show();
    }

    public void backToMainActivity(View v)
    {
        Intent backToMain = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(backToMain);
    }

    public void onClick(View view, int position) {
        Log.d("RankingActivity", "Tap evento on item: "+ position);
    }

    public String encodeToBase64(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        String encImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encImage;
    }
}
