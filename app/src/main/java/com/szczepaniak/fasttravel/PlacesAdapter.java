package com.szczepaniak.fasttravel;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> implements Filterable {

    private ArrayList<String> places;
    private ArrayList<String> allPlaces;
    private LayoutInflater mInflater;
    private ItemClickListener itemClickListener;
    private Context context;
    private String searchText = "";
    private SelectPlacesTypes activity;


    public PlacesAdapter(ArrayList<String> places, SelectPlacesTypes activity) {
        this.places = places;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.mInflater = LayoutInflater.from(context);
        allPlaces = new ArrayList<>(places);
    }

    @Override
    public Filter getFilter() {
        return wordsFilter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.place_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String place = places.get(position);

        int startPos = place.toLowerCase(Locale.US).indexOf(searchText.toLowerCase(Locale.US));
        int endPos = startPos + searchText.length();

        if (startPos != -1) {
            Spannable spannable = new SpannableString(place);
            ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{context.getResources().getColor(R.color.colorPrimaryDark)});
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);
            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.placeText.setText(spannable);
        } else {
            holder.placeText.setText(place);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.addTag(place);
            }
        });

    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView placeText;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            placeText = view.findViewById(R.id.place_text);
        }

        @Override
        public void onClick(View view) {
        }
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private Filter wordsFilter =  new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<String> sortedModels = new ArrayList<>();
            searchText = charSequence.toString().trim();

            if(charSequence == null || charSequence.length() == 0){

                sortedModels.addAll(allPlaces);
            }else {
                String sortedString = charSequence.toString().toLowerCase().trim();
                for(String place  : allPlaces){

                    if(place.toLowerCase().contains(sortedString)){

                        sortedModels.add(place);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = sortedModels;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            places.clear();
            places.addAll((ArrayList)filterResults.values);
            notifyDataSetChanged();
        }
    };
}
