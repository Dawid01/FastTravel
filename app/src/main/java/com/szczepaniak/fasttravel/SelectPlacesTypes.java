package com.szczepaniak.fasttravel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

public class SelectPlacesTypes extends AppCompatActivity {


    private RecyclerView placesView;
    private PlacesAdapter placesAdapter;
   // private TagView tagView;
   TagContainerLayout tagContainerLayout;
   Window window;
   PlacesManager placesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_places_types);


        placesManager = PlacesManager.getInstance(SelectPlacesTypes.this);

        placesView = findViewById(R.id.places);
        placesView.setHasFixedSize(true);
        placesView.setLayoutManager(new LinearLayoutManager(this));
        placesAdapter = new PlacesAdapter(placesManager.getFullPlacesList(), this);
        placesView.setAdapter(placesAdapter);

        tagContainerLayout = (TagContainerLayout) findViewById(R.id.tags);
        tagContainerLayout.setTagMaxLength(20);
        tagContainerLayout.setTagBackgroundColor(getColor(R.color.colorAccent));
        tagContainerLayout.setTagBorderColor(getColor(R.color.colorAccent));
        tagContainerLayout.setTagTextColor(getColor(R.color.colorPrimaryDark));
        tagContainerLayout.setBackgroundColor(Color.TRANSPARENT);
        tagContainerLayout.setBorderColor(Color.TRANSPARENT);
        tagContainerLayout.setCrossColor(getColor(R.color.colorPrimaryDark));
        tagContainerLayout.setEnableCross(true);

        window = getWindow();
        window.setTitle(placesManager.getPlacesList().length + "/8");

        tagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {

            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onSelectedTagDrag(int position, String text) {

            }

            @Override
            public void onTagCrossClick(int position) {
                tagContainerLayout.removeTag(position);
                List<String> tags = tagContainerLayout.getTags();
                placesManager.savePlacesList(tags.toArray(new String[tags.size()]));
                window.setTitle(placesManager.getPlacesList().length + "/8");
            }
        });

        tagContainerLayout.setTags(PlacesManager.getInstance(this).getPlacesList());

    }


    public void addTag(String tag){

        List<String> tags = tagContainerLayout.getTags();
        if(tags.size() < 8){

            boolean canAdd = true;
            for(String s : tags){

                if(tag.equals(s)){
                    canAdd = false;
                    Toast.makeText(this, "This place is already added", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if(canAdd){
                tagContainerLayout.addTag(tag);
            }
        }else {
            Toast.makeText(this, "You can select maximum 8 places", Toast.LENGTH_SHORT).show();
        }

        List<String> places = tagContainerLayout.getTags();
        placesManager.savePlacesList(places.toArray(new String[places.size()]));
        window.setTitle(placesManager.getPlacesList().length + "/8");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.places_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                placesAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }
}