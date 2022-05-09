package com.example.team21_zooseeker.activities.directions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.team21_zooseeker.R;
import com.example.team21_zooseeker.helpers.SharedPrefs;

import java.util.ArrayList;

public class DirectionsActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    Button nextBtn, prevBtn;
    ArrayList<DirectionItem> directions = new ArrayList<DirectionItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        // get views
        viewPager = findViewById(R.id.view_pager);
        nextBtn = findViewById(R.id.next_btn);
        prevBtn = findViewById(R.id.prev_btn);

        directions = SharedPrefs.loadList(this, "directions");

        DirectionsAdapter directionsAdapter = new DirectionsAdapter(directions);
        viewPager.setAdapter(directionsAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setUserInputEnabled(false);

        // set previous btn to be invisible initially
        prevBtn.setVisibility(View.INVISIBLE);
        if (directions.size() == 1)
            nextBtn.setVisibility(View.INVISIBLE);
        else
            nextBtn.setText(directions.get(viewPager.getCurrentItem() + 1).getName());
    }

    public void onNextBtnClicked(View view) {
        int currentIndex = viewPager.getCurrentItem();
        viewPager.setCurrentItem(currentIndex + 1, true);
        setBtnFeatures(currentIndex + 1);
    }

    public void onPrevBtnClicked(View view) {
        int currentIndex = viewPager.getCurrentItem();
        viewPager.setCurrentItem(currentIndex - 1, true);
        setBtnFeatures(currentIndex - 1);
    }

    public void setBtnFeatures(int index) {
        int exhibitCounter = directions.size();
        Log.d("index", String.valueOf(index));

        if (index == exhibitCounter - 1) {
            nextBtn.setVisibility(View.INVISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            prevBtn.setText(directions.get(viewPager.getCurrentItem() - 1).getName());
        }
        else if (index == 0) {
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setText(directions.get(viewPager.getCurrentItem() + 1).getName());
        }
        else {
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            nextBtn.setText(directions.get(viewPager.getCurrentItem() + 1).getName());
            prevBtn.setText(directions.get(viewPager.getCurrentItem() - 1).getName());
        }
    }

    public void onBackBtnClicked(View view) {
        finish();
    }
}