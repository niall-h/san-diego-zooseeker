package com.example.team21_zooseeker.activities.directions;

import static com.example.team21_zooseeker.activities.route.OffTrackCalc.locationUpdate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.team21_zooseeker.R;
import com.example.team21_zooseeker.activities.route.IdentifiedWeightedEdge;
import com.example.team21_zooseeker.activities.route.OffTrackCalc;
import com.example.team21_zooseeker.activities.route.RouteCalc;
import com.example.team21_zooseeker.activities.route.userLocation;
import com.example.team21_zooseeker.helpers.SharedPrefs;
import com.example.team21_zooseeker.helpers.StringFormat;
import com.example.team21_zooseeker.helpers.ZooData;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class DirectionsActivity extends AppCompatActivity {
    private final ExecutorService backgroundThreadExecutor = Executors.newSingleThreadExecutor();
    private Future<Void> future;
    private boolean askForOff = true;

    ViewPager2 viewPager;
    Button nextBtn, prevBtn;
    ToggleButton toggleDesc;
    ArrayList<DirectionItem> detailedDirections = new ArrayList<DirectionItem>();
    ArrayList<DirectionItem> briefDirections = new ArrayList<DirectionItem>();
    DirectionsAdapter directionsAdapter;
    userLocation loc;
    RouteCalc rc;
    StringFormat sf;
    ArrayList<String> exhibits;
    ArrayList<String> userSel;
    //ArrayList<String> userVisited;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        // setup
        {
            loc = new userLocation(this, this);
            rc = new RouteCalc(this);
            sf = new StringFormat(this);
            exhibits = SharedPrefs.loadStrList(this, this.getString(R.string.USER_SELECT));
            userSel = SharedPrefs.loadStrList(this, this.getString(R.string.USER_SELECT));
        }

        // get views
        {
            viewPager = findViewById(R.id.view_pager);
            nextBtn = findViewById(R.id.next_btn);
            prevBtn = findViewById(R.id.prev_btn);
        }

        // gets brief and detailed directions
        {
            briefDirections = SharedPrefs.loadList(this, "directions");
            detailedDirections = SharedPrefs.loadList(this, "detailed_dirs");
        }

        directionsAdapter = new DirectionsAdapter(briefDirections);
        viewPager.setAdapter(directionsAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setUserInputEnabled(false);


        //Toggle event
        {
            ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleDetail);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // The toggle is enabled
                        directionsAdapter.setDirectionsList(detailedDirections);
                        directionsAdapter.notifyDataSetChanged();
                        toggle.setChecked(true);
                    } else {
                        // The toggle is disabled
                        directionsAdapter.setDirectionsList(briefDirections);
                        directionsAdapter.notifyDataSetChanged();
                        toggle.setChecked(false);
                    }
                }
            });

        }

        // set previous btn to be invisible initially
        prevBtn.setVisibility(View.INVISIBLE);

        if (briefDirections.size() == 1)
            nextBtn.setVisibility(View.INVISIBLE);
        else
            nextBtn.setText(briefDirections.get(viewPager.getCurrentItem() + 1).getName());

        // Runs in Background to check if off track
        {
            this.future = backgroundThreadExecutor.submit(() -> {
                do {
                    // updates location
                    runOnUiThread(() -> {
                        locationUpdate(this);
                    });

                    Thread.sleep(5000);
                    // finds next closest exhibit
                    Pair<String, Double> vd = OffTrackCalc.calculateNextClosestExhibit(sf.vInfo, exhibits);

                    // distance of next exhibit
                    Pair<String, Double> curr = OffTrackCalc.
                            distanceToNextExhibit(sf.vInfo,briefDirections.get(viewPager.getCurrentItem()).getId());

                    // change to determine how far one has to be from the next exhibit to
                    // prompt user.
                    // Always greater than 1, or next exhibit will be considered closer even
                    // if its not.
                    double diff_modifier = 1.00;
                    if (vd.second * diff_modifier < curr.second) {
                        if (askForOff) {
                            askForOff = false;
                            runOnUiThread(() -> {
                                Log.d("off-track", vd.first + " " + vd.second +
                                        ", " + curr.first + " " + curr.second);
                                promptOffTrack();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Log.d("not-track", vd.first + " " + vd.second +
                                    ", " + curr.first + " " + curr.second);                        });
                    }
                } while (true);

            });

        }
    }

    public void onNextBtnClicked(View view) {
        removeExhibit();
        int currentIndex = viewPager.getCurrentItem();
        viewPager.setCurrentItem(currentIndex + 1, true);
        setBtnFeatures(currentIndex + 1);


    }

    public void onPrevBtnClicked(View view) {
        int currentIndex = viewPager.getCurrentItem();
        viewPager.setCurrentItem(currentIndex - 1, true);
        setBtnFeatures(currentIndex - 1);
        addExhibit();

    }

    /**
     * onUpdate
     *
     * Called whenever userLocation is set in class userLocation
     * First, determines whether or not the closest exhibit is still
     * the current exhibit
     *
     * If it is, updates the directions on the card
     *
     * If a different exhibit is closer, delegate to Off-Track Suggestions
     * @param id Vertex ID of the user's new location
     */
    public void onUpdate(String id){
        //userSel must be kept updated; if a user has visited particular locations

        int ind = viewPager.getCurrentItem();
        ArrayList<DirectionItem> curr_list = directionsAdapter.getDirectionsList();

        String goal = "";
        for(String key : sf.vInfo.keySet()){
            if(sf.vInfo.get(key).getName().equals(curr_list.get(ind).getName())){
                goal = key;
            }
        }

        GraphPath<String, IdentifiedWeightedEdge> rePath = rc.singleShortestPath(id, goal);

        ArrayList<GraphPath<String, IdentifiedWeightedEdge>> strList = new ArrayList<GraphPath<String, IdentifiedWeightedEdge>>();
        strList.add(rePath);
        List<DirectionItem> strList1 = sf.getDirections(strList, true);
        List<DirectionItem> strList2 = sf.getDirections(strList, false);

        toggleDesc = (ToggleButton) findViewById(R.id.toggleDetail);
        if(toggleDesc.isChecked()){
            curr_list.remove(ind);
            curr_list.add(ind, strList1.get(0));
            directionsAdapter.setDirectionsList(curr_list);
            detailedDirections = curr_list;

            briefDirections.remove(ind);
            briefDirections.add(ind, strList2.get(0));
        }else {
            curr_list.remove(ind);
            curr_list.add(ind, strList2.get(0));
            directionsAdapter.setDirectionsList(curr_list);
            briefDirections = curr_list;

            detailedDirections.remove(ind);
            detailedDirections.add(ind,strList1.get(0));
        }

        directionsAdapter.notifyDataSetChanged();

        GraphPath<String, IdentifiedWeightedEdge> closePath = rc.findNextClosestExhibit(id, userSel);
        if(!closePath.getEndVertex().equals(sf.vInfo.get(curr_list.get(ind).getName()))){
            promptOffTrack();
        }
    }

    public void offTrack(){
        ArrayList<DirectionItem> briefDir = new ArrayList<DirectionItem>();
        ArrayList<DirectionItem> detailedDir = new ArrayList<DirectionItem>();

        ArrayList<DirectionItem> curr_list = directionsAdapter.getDirectionsList();
        ArrayList<String> userVisited = new ArrayList<String>();
        int ind = viewPager.getCurrentItem();
        for(int i = 0; i < ind; i++){
            String goal = "";
            for(String key : sf.vInfo.keySet()){
                if(sf.vInfo.get(key).getName().equals(curr_list.get(i).getName())){
                    goal = key;
                    Log.d("GOAL: ", goal);
                }
            }
            userVisited.add(goal);
        }

        briefDirections.clear();
        detailedDirections.clear();

        if(userVisited.size() > 0){
            ArrayList<String> userVisitedCopy = new ArrayList<String>(userVisited);
            List<GraphPath<String, IdentifiedWeightedEdge>> prevPath = rc.calculateRoute(this.getString(R.string.ENTRANCE_EXIT), userVisitedCopy);

            //removes excess path to end
            prevPath.remove(prevPath.size() -1);

            List<DirectionItem> prevDirsBrief = sf.getDirections(prevPath, false);
            List<DirectionItem> prevDirsDetailed = sf.getDirections(prevPath, true);

            briefDir.addAll(prevDirsBrief);
            detailedDir.addAll(prevDirsDetailed);
        }

        ArrayList<String> userSelCopy = new ArrayList<String>(userSel);
        for(String str: userVisited){
            userSelCopy.remove(str);
        }

        if(userSelCopy.size() > 0){
            String start;
            start = loc.loc_id;

            List<GraphPath<String, IdentifiedWeightedEdge>> currPath = rc.calculateRoute(start, userSelCopy);

            List<DirectionItem> currDirsBrief = sf.getDirections(currPath, false);
            List<DirectionItem> currDirsDetailed = sf.getDirections(currPath, true);

            briefDir.addAll(currDirsBrief);
            detailedDir.addAll(currDirsDetailed);
        }
        else{
            String goal = "";
            for(String key : sf.vInfo.keySet()){
                if(sf.vInfo.get(key).getName().equals(briefDir.get(briefDir.size()-1).getName())){
                    goal = key;
                }
            }
            String current = sf.vInfo.get(goal).id;
            ArrayList<GraphPath<String, IdentifiedWeightedEdge>> lastItem = new ArrayList<GraphPath<String, IdentifiedWeightedEdge>>();
            lastItem.add(DijkstraShortestPath.findPathBetween(rc.g, current, this.getString(R.string.ENTRANCE_EXIT)));

            briefDir.addAll(sf.getDirections(lastItem, false));
            detailedDir.addAll(sf.getDirections(lastItem, true));
        }

        briefDirections = briefDir;
        detailedDirections = detailedDir;

        toggleDesc = (ToggleButton) findViewById(R.id.toggleDetail);
        if(toggleDesc.isChecked()){
            directionsAdapter.setDirectionsList(detailedDirections);
        }
        else {
            directionsAdapter.setDirectionsList(briefDirections);
        }

        directionsAdapter.notifyDataSetChanged();
        setBtnFeatures(ind);
    }

    /*
    * Code for AlertDialog adapted from:
    *  https://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
    */
    public void promptOffTrack(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Off Track!");
        builder.setMessage("You are closer to a different exhibit. Reroute?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                offTrack();
                askForOff = true;

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public void setBtnFeatures(int index) {
        int exhibitCounter = briefDirections.size();
        System.out.println("SIZE: " + exhibitCounter);
        Log.d("index", String.valueOf(index));

        if (index == exhibitCounter - 1) {
            nextBtn.setVisibility(View.INVISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            prevBtn.setText(briefDirections.get(viewPager.getCurrentItem() - 1).getName());
        }
        else if (index == 0) {
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setText(briefDirections.get(viewPager.getCurrentItem() + 1).getName());
        }
        else {
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            nextBtn.setText(briefDirections.get(viewPager.getCurrentItem() + 1).getName());
            prevBtn.setText(briefDirections.get(viewPager.getCurrentItem() - 1).getName());
        }
    }

    public void onSkipBtnClicked(View view){
        ArrayList<DirectionItem> curr_list = directionsAdapter.getDirectionsList();
        int ind = viewPager.getCurrentItem();
        removeExhibit();

        String goal = "";
        for(String key : sf.vInfo.keySet()){
            if(sf.vInfo.get(key).getName().equals(curr_list.get(ind).getName())){
                goal = key;
            }
        }

        userSel.remove(goal);

        if(userSel.size() > 0 && (ind != curr_list.size()-1)) {
            offTrack();
        }
    }

    public void onBackBtnClicked(View view) {
        this.future.cancel(true);
        finish();
    }

    private void removeExhibit() {
        String id = briefDirections.get(viewPager.getCurrentItem()).getId();
        exhibits.remove(id);
    }

    private void addExhibit() {
        String id = briefDirections.get(viewPager.getCurrentItem()).getId();
        exhibits.add(id);
    }

}