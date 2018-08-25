package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.IdeaListHandler;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.JSONResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DateSuggester{
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    private Activity myActivity;
    private RequestQueue myRequestQueue;
    private int myNumRequestsCompleted = 0;
    private List<String> myDateSuggestions;
    private String myTag;

    public DateSuggester(Activity activity, RequestQueue queue, String tag){
        myActivity = activity;
        myRequestQueue = queue;
        myTag = tag;
        myDateSuggestions = new ArrayList<>();
    }

    public void suggest(final IdeaListHandler handler){
        myNumRequestsCompleted = 0;
        myDateSuggestions = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myActivity);
        boolean currentLocationOn = sharedPreferences.getBoolean("current_location_on", true);
        int radius = Integer.parseInt(sharedPreferences.getString("location_radius", "10"));
        final List<String> placeTypes = getPlaceTypes();
        JSONResponseHandler jsonHandler = new JSONResponseHandler() {
            @Override
            public void handleJSONResponse(JSONObject response) {
                myNumRequestsCompleted++;
                List<Map<String,String>> suggestions = null;
                try {
                    suggestions = new DataParser().parseResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                List<String> dates = getListOfDates(suggestions);
                myDateSuggestions.addAll(dates);
                if(myNumRequestsCompleted == placeTypes.size()){
                    new DateIdeaFetcher().getIdeasNotYetEncountered(myDateSuggestions, handler);
                }
            }
        };

        for(String type: placeTypes) {
            if (currentLocationOn) {
                try {
                    new RequestHandler(myActivity, jsonHandler).requestNearbyPlacesInformationCurrentLocation(myRequestQueue, myTag, radius, type);
                } catch (SecurityException e) {
                    ActivityCompat.requestPermissions(myActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_ACCESS_FINE_LOCATION);
                }
            } else {
                new RequestHandler(myActivity, jsonHandler).requestNearbyPlacesInformationOtherLocation(myRequestQueue, myTag, radius, type);
            }
        }
    }

    public List<String> getPlaceTypes(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myActivity);
        String dateCategory = sharedPreferences.getString("date_category", "standard");
        JSONObject categoryToTypes = new JsonAssetLoader().loadJSONFromAsset("categories_to_types.json", myActivity);
        try {
            JSONArray jsonTypes = categoryToTypes.getJSONObject(dateCategory).getJSONArray("types");
            List<String> placeTypes = new ArrayList<>();
            for(int i = 0; i < jsonTypes.length(); i++){
                placeTypes.add(jsonTypes.getString(i));
            }
            Log.w("Place types: ", placeTypes.toString());
            return placeTypes;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    private List<String> getListOfDates(List<Map<String,String>> suggestions){
        List<String> dates = new ArrayList<>();
        for(int i = 0; i < suggestions.size(); i++){
            String placeID = suggestions.get(i).get("id");
            String[] types = suggestions.get(i).get("types").split(DataParser.DEFAULT_DELIMITER);
            for(String activity : new ActivitySuggester().getActivities(types, myActivity)){
                dates.add(placeID + DataParser.DEFAULT_DELIMITER + activity);
            }
        }
        return dates;
    }
}
