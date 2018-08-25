package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import org.json.*;

public class ActivitySuggester {
    public Set<String> getActivities(String[] placeTypes, Context context){
        JSONObject typesToIdeas = new JsonAssetLoader().loadJSONFromAsset("types_to_ideas.json", context);
        Set<String> activities = new HashSet<>();
        try {
            for(String type : placeTypes){
                if(typesToIdeas.has(type)) {
                    JSONArray jsonActivities = typesToIdeas.getJSONArray(type);
                    for(int i = 0; i < jsonActivities.length(); i++){
                        String activity = jsonActivities.getString(i);
                        activities.add(activity);
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return activities;
    }
}
