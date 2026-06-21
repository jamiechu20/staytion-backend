package com.example.demo.util;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

@Component
public class GeocodingService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    public double[] getLatLng(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(URI.create(url), String.class);

            JSONObject json = new JSONObject(response);
            JSONArray results = json.getJSONArray("results");

            if (results.isEmpty()) {
                throw new RuntimeException("查無地址對應的經緯度");
            }

            JSONObject location = results.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");

            return new double[]{lat, lng};

        } catch (Exception e) {
            throw new RuntimeException("取得經緯度失敗: " + e.getMessage());
        }
    }
}
