package com.example.chs_project.ui.map;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import com.google.maps.android.PolyUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteViewModel extends ViewModel {

    private final MutableLiveData<List<LatLng>> routePoints = new MutableLiveData<>();

    public LiveData<List<LatLng>> getRoutePoints() {
        return routePoints;
    }

    public void fetchRoute(LatLng start, LatLng end) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String url = getDirectionsUrl(start, end);
            String response = makeNetworkRequest(url);
            List<LatLng> points = parseRoute(response);
            routePoints.postValue(points);
        });
    }

    private String getDirectionsUrl(LatLng start, LatLng end) {
        String str_origin = "origin=" + start.latitude + "," + start.longitude;
        String str_dest = "destination=" + end.latitude + "," + end.longitude;
        String key = "key=AIzaSyAaBCyG6aDqpEEKY_-D1BbckFTjy4v9XnM";
        String parameters = str_origin + "&" + str_dest + "&" + key;

        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private String makeNetworkRequest(String url) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL apiUrl = new URL(url);
            connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            InputStream stream = new BufferedInputStream(connection.getInputStream());

            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e("TAG", "network request 1 error", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("TAG", "network request 2 error", e);
                }
            }
        }
    }


    private List<LatLng> parseRoute(String jsonResponse) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");
                points = PolyUtil.decode(encodedPolyline);
            }
        } catch (JSONException e) {
            Log.e("TAG", "parse route error", e);
        }
        return points;
    }

}
