package com.diasjoao.metrosultejo.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diasjoao.metrosultejo.data.model.Station;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.diasjoao.metrosultejo.R;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private List<Station> stationList;
    private Set<RecyclerView> timesRecyclerViews = new HashSet<>();
    private boolean isSyncing = false;

    public TimetableAdapter(List<Station> stationList) {
        this.stationList = stationList;
    }

    public static class TimetableViewHolder extends RecyclerView.ViewHolder {
        TextView stopName;
        RecyclerView timesRecyclerView;

        public TimetableViewHolder(@NonNull View itemView) {
            super(itemView);
            stopName = itemView.findViewById(R.id.stopName);
            timesRecyclerView = itemView.findViewById(R.id.timesRecyclerView);
        }
    }

    @NonNull
    @Override
    public TimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_timetable_item, parent, false);
        return new TimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableViewHolder holder, int position) {
        Station station = stationList.get(position);
        holder.stopName.setText(station.getName());

        int totalWidth = (int) holder.stopName.getPaint().measureText("XXXXXXXXXXXXXXX");
        holder.stopName.setWidth(totalWidth);

        /*if (position % 2 == 0) {
            holder.stopName.setBackgroundColor(holder.itemView.getContext().getColor(R.color.colorLightGrey));
        } else {
            holder.stopName.setBackgroundColor(holder.itemView.getContext().getColor(R.color.GhostWhite));
        }*/

        holder.timesRecyclerView.setHasFixedSize(true);
        holder.timesRecyclerView.setLayoutManager(
                new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.timesRecyclerView.setAdapter(new TimeAdapter(station.getConvertedTimes()));

        timesRecyclerViews.add(holder.timesRecyclerView);
        holder.timesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (isSyncing) return;

                isSyncing = true;
                for (RecyclerView syncedRecyclerView : timesRecyclerViews) {
                    if (syncedRecyclerView != recyclerView) {
                        syncedRecyclerView.scrollBy(dx, dy);
                    }
                }
                isSyncing = false;
            }
        });
        /*if (position % 2 == 0) {
            holder.timesRecyclerView.setBackgroundColor(holder.itemView.getContext().getColor(R.color.colorLightGrey));
        } else {
            holder.timesRecyclerView.setBackgroundColor(holder.itemView.getContext().getColor(R.color.GhostWhite));
        }*/
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }
}
