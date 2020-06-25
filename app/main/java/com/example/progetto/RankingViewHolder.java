package com.example.progetto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;

public class RankingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final RankingActivity rankingActivity;
    private TextView pos, user, lp, xp;
    private ImageView img;

    public RankingViewHolder(@NonNull View itemView, RankingActivity rankingActivity) {
        super(itemView);

        pos = itemView.findViewById(R.id.pos);
        user = itemView.findViewById(R.id.username);
        lp = itemView.findViewById(R.id.lifepoints);
        xp = itemView.findViewById(R.id.experience);
        img = itemView.findViewById(R.id.player_image);
        itemView.setOnClickListener(this);
        this.rankingActivity = rankingActivity;
    }

    public void setText(String p, String us, String i, String l, String x){
        pos.setText(p);
        img.setImageBitmap(decodeFromBase64(i));
        user.setText(us);
        lp.setText(l);
        xp.setText(x);
    }

    @Override
    public void onClick(View view) {
        Log.d("RankViewHolder", "Click on item: "+ user.getText().toString()+" with position: "+getAdapterPosition());
        rankingActivity.onClick(view, getAdapterPosition());
    }

    public Bitmap decodeFromBase64(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();

        try {
            imageBytes = Base64.decode(s, Base64.DEFAULT);
        } catch (Exception e){}

        Bitmap decodeImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return decodeImage;
    }
}
