package com.startandroid.develop.globalweatherapp;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends FragmentActivity implements OnMapReadyCallback {
    //Ресурс парсинга городов для выпадающего списка
    private String URL = "https://raw.githubusercontent.com/aZolo77/citiesBase/master/cities.json";
    private FirebaseAuth firebaseAuth;
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    Marker marker;
    String City_map;
    Button Get_weather;
    String city;
    String lat = null;
    String lon = null;
    String lat_spinner = null;
    String lon_spinner = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //находим все елементы activity weather
        searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        Get_weather = findViewById(R.id.get_weather_but);
        final ArrayList<String> cityNames = getCountries(URL);
        final Spinner spinner = findViewById(R.id.country_Name);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                city = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner.setAdapter(new ArrayAdapter<>(WeatherActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                cityNames));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = searchView.getQuery().toString();
                List<Address> addressesList = null;
                //Проверяем не пустое ли значение поиска
                if(location != null ||!location.equals("")){
                    Geocoder geocoder = new Geocoder(WeatherActivity.this);
                    try {
                        addressesList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressesList.get(0);
                    //получаем долготу и широту
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    City_map = addressesList.get(0).getLocality();
                    lat = String.valueOf(address.getLatitude());
                    lon = String.valueOf(address.getLongitude());
                    //удаляем приведущий маркер
                    if(marker != null){
                        marker.remove();
                    }
                    //добавляем метку на карте и отображаем название города
                    marker = map.addMarker(new MarkerOptions().position(latLng).title(City_map));
                    //анимация зума камеры на маркер
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        Get_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            //Создаем слушатель на кнопку
            public void onClick(View view) {
                //проверка для выпадающего списка
                if(city !="Выберете город...") {
                    List<Address> addressesList_spinner = null;
                    Geocoder geocoder_spinner = new Geocoder(WeatherActivity.this);
                    try {
                        addressesList_spinner = geocoder_spinner.getFromLocationName(city,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address_spinner = addressesList_spinner.get(0);
                    //получаем долготу и широту
                    lat_spinner = String.valueOf(address_spinner.getLatitude());
                    lon_spinner = String.valueOf(address_spinner.getLongitude());
                    Toast toast_wait = Toast.makeText(getApplicationContext(),
                            "Подождите, загрузка погоды...", Toast.LENGTH_LONG);
                    toast_wait.show();
                    Intent i = new Intent(getApplicationContext(), GetWeatherActivity.class);
                    //передаем значение из этого окна в другое
                    i.putExtra("City_name_activity", city);
                    i.putExtra("Lat",lat_spinner );
                    i.putExtra("Lon",lon_spinner );
                    //запускаем новое окно
                    startActivity(i);
                }
                else {
                    //проверка для гугл карты
                    if(marker!=null) {
                        Toast toast_wait = Toast.makeText(getApplicationContext(),
                                "Подождите, загрузка погоды...", Toast.LENGTH_LONG);
                        toast_wait.show();
                        Intent i = new Intent(getApplicationContext(), GetWeatherActivity.class);
                        //передаем значение из этого окна в другое
                        i.putExtra("City_name_activity", City_map);
                        i.putExtra("Lat",lat);
                        i.putExtra("Lon",lon);
                        //запускаем новое окно
                        startActivity(i);
                    }
                }
            }
        });
    }


//функция проверки авторизации пользователя
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            // пользователь не вошел
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }
    //метод после загрузки карты
    @Override
    public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    //очищаем карту
    map.clear();
    }

    //Парсим список городов
    public static ArrayList<String> getCountries(String url) {
        ArrayList<String> arrayList = null;
        try { arrayList = new NetworkAsyncTask().execute(url).get(); }
        catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        return arrayList;
    }


    //Класс для асинхронного парсинга (в нём, по сути, парсинг и выполняется)
    static class NetworkAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> tempList = new ArrayList<>();
            tempList.add("Выберете город...");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(strings[0])
                        .build();
                Response responses = null;
                try { responses = client.newCall(request).execute(); }
                catch (IOException e) { e.printStackTrace(); }
                String jsonData = responses.body().string();
                JSONObject Jobject = new JSONObject(jsonData);
                JSONArray Jarray = Jobject.getJSONArray("city");
                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject object = Jarray.getJSONObject(i);
                    String item = object.getString("name");
                    tempList.add(item);
                }
            } catch (JSONException | IOException e) { e.printStackTrace(); }
            return tempList;
        }
    };
}
