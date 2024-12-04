package com.example.ejercicio_3_map.main.repos.firebase;

import android.util.Log;

import com.example.ejercicio_3_map.main.model.Farmacia;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FarmaciaHandler {

    private static final String API_URL = "https://www.zaragoza.es/sede/servicio/farmacia.json";

    public interface FarmaciaCallback {
        void onSuccess(List<Farmacia> farmacias);
        void onError(String error);
    }

    public void obtenerFarmacias(FarmaciaCallback callback) {
        new Thread(() -> {
            try {
                // Conectar con la API
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Leer la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parsear los datos JSON
                List<Farmacia> farmacias = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray jsonArray = jsonResponse.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject farmaciaJson = jsonArray.getJSONObject(i);

                    // Maneja casos donde address no existe
                    String direccion = farmaciaJson.has("address") ? farmaciaJson.getString("address") : "Sin dirección disponible";

                    Farmacia farmacia = new Farmacia(
                            farmaciaJson.getString("id"),
                            farmaciaJson.getString("title"),
                            direccion,
                            farmaciaJson.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1),
                            farmaciaJson.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
                    );
                    farmacias.add(farmacia);
                }

                callback.onSuccess(farmacias);
            } catch (Exception e) {
                Log.e("FarmaciaHandler", "Error al obtener farmacias", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
