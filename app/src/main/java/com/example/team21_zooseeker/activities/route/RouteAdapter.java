package com.example.team21_zooseeker.activities.route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team21_zooseeker.R;
import com.example.team21_zooseeker.helpers.ExhibitEntity;

import java.util.Collections;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder>{

    //private List<ExhibitDistance> directions = Collections.emptyList();
    private List<String> directions = Collections.emptyList();

    public void setDirections(List<String> directions){
        this.directions.clear();
        this.directions = directions;
        // notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.exhibit_distance, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setExhibitText(directions.get(position));
        holder.exhibitCounter.setText("Exhibit " + (position + 1) + ": ");
    }

    @Override
    public int getItemCount() {
        return directions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final TextView exhibitCounter;
        //private ExhibitDistance exhibitDist;
        private String exhibitDist;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            this.textView = itemView.findViewById(R.id.selected_exhibit_name);
            this.exhibitCounter = itemView.findViewById(R.id.exhibit_title_route);
        }

        public String getExhibit(){
            return exhibitDist;
        }

        public void setExhibitText(String exdst){
            exhibitDist = exdst;
            this.textView.setText(exdst);
        }

        public String getExhibitDist(){
            return exhibitDist;
        }
    }

}

