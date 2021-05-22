package com.startandroid.develop.globalweatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class GetWeatherActivity extends AppCompatActivity{
    String City = null;
    String lat = null;
    String lon = null;
    //Ключ openweathermap
    String Key = "f0d8819db15df1b47132ed74da2a3770" ;
    TextView txtCity,txtDate,txtValueTemp,txtValueHumidy,txtValueFeelLike,txtValuePressure,txtValueWind;
    TextView ValueTempHourly_one, ValueTempHourly_two,ValueTempHourly_three,ValueTempHourly_four,ValueDateHourly_one,
            ValueDateHourly_two,ValueDateHourly_three,ValueDateHourly_four;
    TextView ValueWindHourly_one,ValueWindHourly_two,ValueWindHourly_three,ValueWindHourly_four;
    ImageView Status_weather_hourly_one,Status_weather_hourly_two,Status_weather_hourly_three,Status_weather_hourly_four;
    TextView Value_temp_dayly_day_one,Value_temp_dayly_night_one,Value_temp_dayly_day_two,Value_temp_dayly_night_two,
            Value_temp_dayly_day_three,Value_temp_dayly_night_three,Value_temp_dayly_day_four,Value_temp_dayly_night_four;
    TextView Value_TimeDaylyone,Value_TimeDaylytwo,Value_TimeDaylythree,Value_TimeDaylyfour;
    ImageView Status_dayly_one,Status_dayly_two,Status_dayly_three,Status_dayly_four;
    ImageView Status_weather_icon;
    //Создаем списки для хранения и вывода информации по погоде
    ArrayList<Double> temp_all_hourly = new ArrayList<Double>();
    ArrayList<Long> date_all_hourly = new ArrayList<Long>();
    ArrayList<String> img_all_hourly = new ArrayList<String>();
    ArrayList<Double> wind_all_hourly = new ArrayList<>();
    ArrayList<Double> temp_day_dayly_all = new ArrayList<Double>();
    ArrayList<Double> temp_night_dayly_all = new ArrayList<Double>();
    ArrayList<Long> date_dayly_all = new ArrayList<Long>();
    ArrayList<String> img_dayly_all = new ArrayList<String>();
    //Переменные форматирования, округления
    DecimalFormat f;
    DecimalFormat d;
    DecimalFormat c;
    //Создаем переменные для создания потока
    private Thread secThread;
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_weather);
        //Вызываем альтернативный поток
        init();
        //Получаем переменную города и координат из преведущего окна WeatherActivity
        Bundle extras = getIntent().getExtras();
        if(extras!= null){
            City = extras.getString("City_name_activity");
            lat = extras.getString("Lat");
            lon = extras.getString("Lon");
        }
        //Вызывем функцию получения текущей погоды
        GetCurrentWeather(City);
        //Вызывем функцию получения погоды почасово
        GetWeatherHourly(lat,lon);
        //Вызывем функцию получения погоды посуточно
        GetWeatherDayly(lat,lon);
    }
    // Функция создания альтернативного потока
    private void init(){
        runnable = new Runnable() {
            @Override
            public void run() {
                //указываем операцию которая будет происходить в потоке
                LoadFragment();
            }
        };
        secThread = new Thread(runnable);
        //Запуск потока
        secThread.start();
    }
    //Находим все елементы окна
    public void LoadFragment(){
        txtValueTemp = findViewById(R.id.Temp_text);
        txtCity = findViewById(R.id.nameCity);
        txtValueHumidy = findViewById(R.id.Valuehumidy);
        txtValueFeelLike =findViewById(R.id.Valuefellslike);
        txtValuePressure = findViewById(R.id.Valuepreassure);
        txtValueWind = findViewById(R.id.Valuewind);
        txtDate= findViewById(R.id.Time_value);
        Status_weather_icon = findViewById(R.id.Current_weather_icon);
        ValueTempHourly_one = findViewById(R.id.temp_hourly_one);
        ValueTempHourly_two = findViewById(R.id.temp_hourly_two);
        ValueTempHourly_three = findViewById(R.id.temp_hourly_three);
        ValueTempHourly_four = findViewById(R.id.temp_hourly_four);
        ValueDateHourly_one = findViewById(R.id.time_hourly_one);
        ValueDateHourly_two = findViewById(R.id.time_hourly_two);
        ValueDateHourly_three = findViewById(R.id.time_hourly_three);
        ValueDateHourly_four = findViewById(R.id.time_hourly_four);
        Status_weather_hourly_one = findViewById(R.id.hourly_img_status_one);
        Status_weather_hourly_two = findViewById(R.id.hourly_img_status_two);
        Status_weather_hourly_three = findViewById(R.id.hourly_img_status_three);
        Status_weather_hourly_four = findViewById(R.id.hourly_img_status_four);
        ValueWindHourly_one = findViewById(R.id.wind_hourly_one);
        ValueWindHourly_two = findViewById(R.id.wind_hourly_two);
        ValueWindHourly_three = findViewById(R.id.wind_hourly_three);
        ValueWindHourly_four = findViewById(R.id.wind_hourly_four);
        Value_temp_dayly_day_one = findViewById(R.id.temp_dayly_day_one);
        Value_temp_dayly_day_two = findViewById(R.id.temp_dayly_day_two);
        Value_temp_dayly_day_three = findViewById(R.id.temp_dayly_day_three);
        Value_temp_dayly_day_four = findViewById(R.id.temp_dayly_day_four);
        Value_temp_dayly_night_one = findViewById(R.id.temp_dayly_night_one);
        Value_temp_dayly_night_two = findViewById(R.id.temp_dayly_night_two);
        Value_temp_dayly_night_three = findViewById(R.id.temp_dayly_night_three);
        Value_temp_dayly_night_four = findViewById(R.id.temp_dayly_night_four);
        Value_TimeDaylyone = findViewById(R.id.time_dayly_one);
        Value_TimeDaylytwo = findViewById(R.id.time_dayly_two);
        Value_TimeDaylythree = findViewById(R.id.time_dayly_three);
        Value_TimeDaylyfour = findViewById(R.id.time_dayly_four);
        Status_dayly_one = findViewById(R.id.dayly_img_status_one);
        Status_dayly_two = findViewById(R.id.dayly_img_status_two);
        Status_dayly_three = findViewById(R.id.dayly_img_status_three);
        Status_dayly_four = findViewById(R.id.dayly_img_status_four);
    }
    //Функция округления значений ветра и температуры для текущей погоды
    public void RoundingValue_Current(Double value_not_rounding){
         f = new DecimalFormat("##");
    }
    //Функция получения текущей погоды
    public void GetCurrentWeather (String City){
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + City +"&units=metric&lang=ru&appid=" + Key;
        DownloadJSON downloadJSON = new DownloadJSON();
        try {
            String result = "abc";
            result = downloadJSON.execute(url).get();
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");
            Double temp = main.getDouble("temp");
            Double feel_like = main.getDouble("feels_like");
            String humidity = main.getString("humidity");
            String preassure = main.getString("pressure");
            Double wind = jsonObject.getJSONObject("wind").getDouble("speed");
            String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            RoundingValue_Current(temp);
            RoundingValue_Current(feel_like);
            RoundingValue_Current(wind);
            txtCity.setText(City);
            txtDate.setText(description);
            txtValueTemp.setText(f.format(temp) +"°");
            txtValueHumidy.setText(humidity+"%");
            txtValueFeelLike.setText(f.format(feel_like)+"°");
            txtValuePressure.setText(preassure+"hPa");
            txtValueWind.setText(f.format(wind)+" м/с");
            String nameIcon = "10d";
            nameIcon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            String urlIcon = "http://openweathermap.org/img/wn/"+nameIcon+"@2x.png";
            DownloadIcon downloadIcon = new DownloadIcon();
            Bitmap bitmap = downloadIcon.execute(urlIcon).get();
            Status_weather_icon.setImageBitmap(bitmap);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //Функция переобразования даты почасовой погоды
    public void FormatDate_Hourly(Long Time_one_hour, Long Time_two_hour, Long Time_three_hour, Long Time_four_hour){
        Date time_one_hourly = new Date(Time_one_hour*1000);
        Date time_two_hourly = new Date(Time_two_hour*1000);
        Date time_three_hourly = new Date(Time_three_hour*1000);
        Date time_four_hourly = new Date(Time_four_hour*1000);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm",Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        ValueDateHourly_one.setText(dateFormat.format(time_one_hourly));
        ValueDateHourly_two.setText(dateFormat.format(time_two_hourly));
        ValueDateHourly_three.setText(dateFormat.format(time_three_hourly));
        ValueDateHourly_four.setText(dateFormat.format(time_four_hourly));
    }
    //Функция округления значений ветра и температуры для почасовой погоды
    public void RoundingValue_Hourly(Double value_rounding){
        d = new DecimalFormat("##");
    }
    //Функция получения погоды почасово
    public void GetWeatherHourly(String lat,String lon){
        String url_hourly = "https://api.openweathermap.org/data/2.5/onecall?lat=" +lat+"&lon="+lon+"&units=metric&lang=ru&appid=" + Key;
        DownloadJSON downloadJSON_hourly = new DownloadJSON();
        try {
            String result_three_hourly = "abc";
            String nameIcon_hourly = "10d";
            result_three_hourly = downloadJSON_hourly.execute(url_hourly).get();
            JSONObject jsonObject = new JSONObject(result_three_hourly);
            JSONArray list = jsonObject.getJSONArray("hourly");
            for(int i = 1; i<5; i++) {
                Double temp_hourly = list.getJSONObject(i).getDouble("temp");
                Long time_hourly = list.getJSONObject(i).getLong("dt");
                nameIcon_hourly = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
                Double wind_hourly = list.getJSONObject(i).getDouble("wind_speed");
                RoundingValue_Hourly(temp_hourly);
                img_all_hourly.add(nameIcon_hourly);
                date_all_hourly.add(time_hourly);
                temp_all_hourly.add(temp_hourly);
                wind_all_hourly.add(wind_hourly);
            }
            ValueTempHourly_one.setText(d.format(temp_all_hourly.get(0))+"°");
            ValueTempHourly_two.setText(d.format(temp_all_hourly.get(1))+"°");
            ValueTempHourly_three.setText(d.format(temp_all_hourly.get(2))+"°");
            ValueTempHourly_four.setText(d.format(temp_all_hourly.get(3))+"°");
            ValueWindHourly_one.setText(d.format(wind_all_hourly.get(0))+" м/с");
            ValueWindHourly_two.setText(d.format(wind_all_hourly.get(1))+" м/с");
            ValueWindHourly_three.setText(d.format(wind_all_hourly.get(2))+" м/с");
            ValueWindHourly_four.setText(d.format(wind_all_hourly.get(3))+" м/с");
            FormatDate_Hourly(date_all_hourly.get(0),date_all_hourly.get(1),date_all_hourly.get(2),date_all_hourly.get(3));
            String urlIcon_hourly_one = "http://openweathermap.org/img/wn/" + img_all_hourly.get(0) + "@2x.png";
            String urlIcon_hourly_two = "http://openweathermap.org/img/wn/" + img_all_hourly.get(1) + "@2x.png";
            String urlIcon_hourly_three = "http://openweathermap.org/img/wn/" + img_all_hourly.get(2) + "@2x.png";
            String urlIcon_hourly_four = "http://openweathermap.org/img/wn/" + img_all_hourly.get(3) + "@2x.png";
            DownloadIcon downloadIcon_hourly_one = new DownloadIcon();
            Bitmap bitmap_one = downloadIcon_hourly_one.execute(urlIcon_hourly_one).get();
            Status_weather_hourly_one.setImageBitmap(bitmap_one);
            DownloadIcon downloadIcon_hourly_two = new DownloadIcon();
            Bitmap bitmap_two = downloadIcon_hourly_two.execute(urlIcon_hourly_two).get();
            Status_weather_hourly_two.setImageBitmap(bitmap_two);
            DownloadIcon downloadIcon_hourly_three = new DownloadIcon();
            Bitmap bitmap_three = downloadIcon_hourly_three.execute(urlIcon_hourly_three).get();
            Status_weather_hourly_three.setImageBitmap(bitmap_three);
            DownloadIcon downloadIcon_hourly_four = new DownloadIcon();
            Bitmap bitmap_four = downloadIcon_hourly_four.execute(urlIcon_hourly_four).get();
            Status_weather_hourly_four.setImageBitmap(bitmap_four);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //Функция переобразования даты посуточной погоды
    public void FormatDate_Dayly(Long Time_one_dayly, Long Time_two_dayly, Long Time_three_dayly, Long Time_four_dayly){
        Date time_one_dayly = new Date(Time_one_dayly*1000);
        Date time_two_dayly = new Date(Time_two_dayly*1000);
        Date time_three_dayly = new Date(Time_three_dayly*1000);
        Date time_four_dayly = new Date(Time_four_dayly*1000);
        Locale localeRu = new Locale("ru", "RU");
        DateFormat dateFormat = new SimpleDateFormat("EEEE",localeRu);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        Value_TimeDaylyone.setText(dateFormat.format(time_one_dayly));
        Value_TimeDaylytwo.setText(dateFormat.format(time_two_dayly));
        Value_TimeDaylythree.setText(dateFormat.format(time_three_dayly));
        Value_TimeDaylyfour.setText(dateFormat.format(time_four_dayly));
    }
    //Функция округления значений ветра и температуры для посуточной погоды
    public void RoundingValue_Dayly(Double value_dayly_rounding){
        c = new DecimalFormat("##");
    }
    //Функция получения погоды посуточно
    public void GetWeatherDayly(String lat,String lon){
        String url_dayly = "https://api.openweathermap.org/data/2.5/onecall?lat=" +lat+"&lon="+lon+"&units=metric&lang=ru&appid=" + Key;
        DownloadJSON downloadJSON_dayly = new DownloadJSON();
        try {
            String result_dayly = "abc";
            String nameIcon_dayly = "10d";
            result_dayly = downloadJSON_dayly.execute(url_dayly).get();
            JSONObject jsonObject = new JSONObject(result_dayly);
            JSONArray list = jsonObject.getJSONArray("daily");
            for(int i = 1; i<5; i++) {
                Long time_dayly = list.getJSONObject(i).getLong("dt");
                date_dayly_all.add(time_dayly);
                Double temp_dayly_day = list.getJSONObject(i).getJSONObject("temp").getDouble("day");
                Double temp_dayly_night = list.getJSONObject(i).getJSONObject("temp").getDouble("night");
                RoundingValue_Dayly(temp_dayly_day);
                RoundingValue_Dayly(temp_dayly_night);
                temp_day_dayly_all.add(temp_dayly_day);
                temp_night_dayly_all.add(temp_dayly_night);
                nameIcon_dayly = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
                img_dayly_all.add(nameIcon_dayly);
            }
            Value_temp_dayly_day_one.setText(c.format(temp_day_dayly_all.get(0))+ "°/");
            Value_temp_dayly_day_two.setText(c.format(temp_day_dayly_all.get(1))+ "°/");
            Value_temp_dayly_day_three.setText(c.format(temp_day_dayly_all.get(2))+ "°/");
            Value_temp_dayly_day_four.setText(c.format(temp_day_dayly_all.get(3))+ "°/");
            Value_temp_dayly_night_one.setText(c.format(temp_night_dayly_all.get(0))+ "°");
            Value_temp_dayly_night_two.setText(c.format(temp_night_dayly_all.get(1))+ "°");
            Value_temp_dayly_night_three.setText(c.format(temp_night_dayly_all.get(2))+ "°");
            Value_temp_dayly_night_four.setText(c.format(temp_night_dayly_all.get(3))+ "°");
            FormatDate_Dayly(date_dayly_all.get(0),date_dayly_all.get(1),date_dayly_all.get(2),date_dayly_all.get(3));
            String urlIcon_dayly_one = "http://openweathermap.org/img/wn/" + img_dayly_all.get(0) + "@2x.png";
            String urlIcon_dayly_two = "http://openweathermap.org/img/wn/" + img_dayly_all.get(1) + "@2x.png";
            String urlIcon_dayly_three = "http://openweathermap.org/img/wn/" + img_dayly_all.get(2) + "@2x.png";
            String urlIcon_dayly_four = "http://openweathermap.org/img/wn/" + img_dayly_all.get(3) + "@2x.png";
            DownloadIcon downloadIcon_dayly_one = new DownloadIcon();
            Bitmap bitmap_one_dayly = downloadIcon_dayly_one.execute(urlIcon_dayly_one).get();
            Status_dayly_one.setImageBitmap(bitmap_one_dayly);
            DownloadIcon downloadIcon_dayly_two = new DownloadIcon();
            Bitmap bitmap_two_dayly = downloadIcon_dayly_two.execute(urlIcon_dayly_two).get();
            Status_dayly_two.setImageBitmap(bitmap_two_dayly);
            DownloadIcon downloadIcon_dayly_three = new DownloadIcon();
            Bitmap bitmap_three_dayly = downloadIcon_dayly_three.execute(urlIcon_dayly_three).get();
            Status_dayly_three.setImageBitmap(bitmap_three_dayly);
            DownloadIcon downloadIcon_dayly_four = new DownloadIcon();
            Bitmap bitmap_four_dayly = downloadIcon_dayly_four.execute(urlIcon_dayly_four).get();
            Status_dayly_four.setImageBitmap(bitmap_four_dayly);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
