package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataParser {
    public static final String DEFAULT_DELIMITER = ":::";

    public List<Map<String, String>> parseResponse(JSONObject response) throws JSONException{
        List<Map<String, String>> placeInformation = new ArrayList<>();
        JSONArray results = response.getJSONArray("results");
        for(int resultsIdx = 0; resultsIdx < results.length(); resultsIdx++){
            placeInformation.add(parsePlaceInformation(results.getJSONObject(resultsIdx)));
        }
        return placeInformation;
    }

    private Map<String, String> parsePlaceInformation(JSONObject place) throws JSONException{
        Map<String, String> placeInfo = new HashMap<>();
        JSONObject localityInfo = place.getJSONObject("geometry").getJSONObject("location");
        placeInfo.put("lat", "" + localityInfo.getDouble("lat"));
        placeInfo.put("lng", "" + localityInfo.getDouble("lng"));
        placeInfo.put("id", place.getString("place_id"));
        String places = "";
        JSONArray arr = place.getJSONArray("types");
        for(int i = 0; i < arr.length() - 1; i++){
            places = places + arr.getString(i) + DEFAULT_DELIMITER;
        }
        places = places + arr.getString(arr.length() - 1);
        placeInfo.put("types", places);
        return placeInfo;
    }
}
