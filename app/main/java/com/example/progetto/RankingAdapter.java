package com.example.progetto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RankingAdapter extends RecyclerView.Adapter<RankingViewHolder> {
    private final RankingActivity rankingActivity;
    private LayoutInflater rankInflater;

    public RankingAdapter(RankingActivity rankingActivity) {
        this.rankInflater = LayoutInflater.from(rankingActivity);
        this.rankingActivity = rankingActivity;
    }

    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = rankInflater.inflate(R.layout.ranking_singlerow, parent, false);
        return new RankingViewHolder(view, rankingActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Player p = PlayerModel.getInstance().getPlayer(position);
        String pos = String.valueOf(position+1);
        holder.setText(pos, p.getUsername(), p.getImg(), p.getLifepoints(), p.getExperience());
    }

    @Override
    public int getItemCount() {
        return PlayerModel.getInstance().getSize();
    }
}
