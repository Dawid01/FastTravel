package com.szczepaniak.fasttravel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlacesManager {

    private static PlacesManager placesManager;
    private static SharedPreferences sharedPref;
    private final String PLACES_KEY = "USED_PLACES";

    private HashSet<String> defaultPlaces = new HashSet<>();


    private PlacesManager(){

        defaultPlaces.add("amusement park, theme park");
        defaultPlaces.add("museum");
        defaultPlaces.add("restaurant");
        defaultPlaces.add("pizza, restaurant");
        defaultPlaces.add("gym");
        defaultPlaces.add("theater, movie theater, cinema");
        defaultPlaces.add("college, university");
        defaultPlaces.add("zoo, aquarium, wildlife sanctuary");

    }

    public static PlacesManager getInstance(Activity activity) {
        if(placesManager == null){
            placesManager = new PlacesManager();
            sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        }
        return placesManager;
    }

    private String[] fullPlacesList = {
            "advertising, marketing",
            "airport",
            "amusement park, theme park",
            "assisted living service, assisted, assisted living, home",
            "athletic field, sports field",
            "bagel, donut",
            "bar, alcohol",
            "base, military",
            "beach",
            "beauty, hair, salon, barber",
            "beer, wine, spirit, alcohol, booze",
            "bicycle, bike, cycle",
            "boating",
            "bookstore, book shop",
            "bowling, bowl, bowling alley",
            "brewery, beer",
            "bus station, bus stop, bus",
            "campground, rv park",
            "candy store, candy, candies, confectionary, chocolatier, chocolate",
            "car rental, truck rental",
            "car wash, detail, car detail, car wax",
            "casinos, gaming",
            "cemetery, graveyard, mausoleum",
            "clothing, accessories, apparel",
            "college, university",
            "combat sports, boxing, martial arts, fighting",
            "computer, electronic",
            "contractor, repair",
            "dance",
            "day care, preschool, daycare, child care",
            "department store, big box store, department",
            "dog park",
            "drug services, alcohol services, clinic",
            "dry cleaning, laundry, dry cleaner, laundry service, laundromat",
            "education, school",
            "embassy, foreign",
            "er, emergency room",
            "fast food",
            "fire stations, fire house, fire department",
            "florist, flowers, flower shop",
            "forest, woods",
            "funeral service, funeral home",
            "furniture, decor",
            "garden",
            "gas station, fuel, gas",
            "gift, novelty",
            "glasses, optical",
            "government agency",
            "gun range",
            "home improvement, repairman, handyman, repair",
            "hospital, clinic, medical center",
            "hotel, motel",
            "houseware, home goods",
            "ice cream parlor, ice cream",
            "insurance",
            "jewelry, watches, accessories",
            "juice bar, smoothie, juice",
            "lake",
            "landmark",
            "legal, lawyer, law, law office",
            "lodging",
            "massage, masseuse",
            "meat, seafood, butcher, deli",
            "miniature golf, minigolf",
            "mountain",
            "museum",
            "music, show venue, concert, concert hall",
            "natural park",
            "newsstand, newspaper, news, magazine",
            "night club, disco",
            "notary, notary public",
            "nursery, garden, garden center",
            "outdoors",
            "painting, art",
            "park",
            "parking, parking lot",
            "pet, petshop, dog, cat",
            "pharmacy",
            "photo, frame, framing",
            "photography, photo service",
            "physical therapy, rehabilitation",
            "pizza, restaurant",
            "police station, law enforcement",
            "port, ferry",
            "post office, mail",
            "professional cleaning, cleaning",
            "rail station, train station",
            "real estate agent, realtor, real estate agency",
            "rest area, rest stop, pitstop",
            "restaurant",
            "scuba diving, pool",
            "shoes, apparel",
            "shopping center, mall, shopping mall",
            "spa",
            "sporting good, sports store",
            "storage, storage facility, storage lot",
            "supermarket, groceries, grocery, market, super",
            "swimming pool, pool, swim club",
            "tailor",
            "tattoo, tattooing",
            "theater, movie theater, cinema",
            "tour, travel agent",
            "tourist information, services, tourism, information",
            "toy, toy shop",
            "utility companies, utilities, utility, public utility, electricity, natural gas, water, sewage, power company",
            "vehicle maintenance, car maintenance, vehicle repair, car repair",
            "winery, vineyard, wine tasting",
            "zoo, aquarium, wildlife sanctuary",
            "gym"

    };

    public ArrayList<String> getFullPlacesList() {
        ArrayList<String> places = new ArrayList<>();
        for (String str : fullPlacesList)
            places.add(str);
        return places;
    }

    public String[] getPlacesList() {
        Set<String> places = sharedPref.getStringSet(PLACES_KEY, defaultPlaces);
        return places.toArray(new String[places.size()]);
    }

    public void savePlacesList(String[] places){
        Set<String> placesToSave = new HashSet<String>(Arrays.asList(places));
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(PLACES_KEY, placesToSave);
        editor.apply();
    }
}
