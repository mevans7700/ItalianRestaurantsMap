package com.evansappwriter.italianrestaurantsmap.core;

import com.evansappwriter.italianrestaurantsmap.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Parser {
    private static final String TAG = "PARSER";

    public static final int TYPE_PARSER_ERROR = -1;
    public static final int TYPE_PARSER_NONE = 0;
    public static final int TYPE_PARSER_RESTAURANTS = 1;

    // this class cannot be instantiated
    private Parser() {

    }

    public static void parseResponse(BundledData data) {
        int parserType = data.getParserType();

        Utils.printLogInfo(TAG, data.getHttpData());

        switch (parserType) {
            case TYPE_PARSER_RESTAURANTS:
                parseRestaurants(data);
                break;
            case TYPE_PARSER_ERROR:
                parseError(data);
                break;
            case TYPE_PARSER_NONE:
            default:
                // no parse needed
                break;
        }
    }

    private static void parseRestaurants(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData();
            return;
        }

        try {

            // starting to parse...
            JSONObject jObject = new JSONObject(data.getHttpData());

            JSONArray jPlaces = jObject.getJSONArray("results");

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);

            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<>();
            HashMap<String, String> place = null;

            /** Taking each place, parses and adds to list object */
            for(int i=0; i<placesCount;i++){
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);
            }

            data.setAuxData(placesList);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setHttpData(null);
            data.setAuxData();
        }
    }

    /** Parsing the Place JSON object */
    private static HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<>();
        String placeName = "-NA-";
        String vicinity="-NA-";
        String snippet="";
        String latitude="";
        String longitude="";


        try {
            // Extracting Place name, if available
            if(!jPlace.isNull("name")){
                placeName = jPlace.getString("name");
            }

            // Extracting Place Vicinity, if available
            if(!jPlace.isNull("formatted_address")){
                vicinity = jPlace.getString("formatted_address");
            }

            if (!jPlace.isNull("opening_hours")) {
                snippet = "Open Now: " + (jPlace.getJSONObject("opening_hours").getBoolean("open_now") ? "Open" : "Closed");
            }


            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");


            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("snippet", snippet);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

    private static void parseError(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData("Bad Payload", data.getHttpData());
            return;
        }

        try {
            JSONObject json = new JSONObject(data.getHttpData());

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);



            data.setAuxData();
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setAuxData("Server Error", data.getHttpData());
            data.setHttpData(null);
        }
    }

    // useful methods

    private static String getStringObject(String txt) {
        return txt == null ? null : txt.equalsIgnoreCase("null") ? null : txt;
    }

    // KEYS >>>>>>>>>

    private static final String KEY_ERROR = "error";
    private static final String KEY_ERROR_DETAIL = "error_detail";
}
