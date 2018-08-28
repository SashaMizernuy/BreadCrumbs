package ua.genesis.sasha.breadcrumbs;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.RequestQueue;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleMap Map;

    private CameraPosition mCameraPosition;

    // Точка входу Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private RequestQueue reQueue;

    // Точка входу Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // За замовчуванням location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // Географічне положення, де зараз знаходиться пристрій. Тобто останнім відомі місце, отримане постачальником Fused Location.
    private Location mLastKnownLocation;

    // Ключі для зберігання активності.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Використовується для вибору поточного місця.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;


    Polyline line;

    TextView edDistance;
    TextView edDuration;
    SharedPreferences mSettings;
    public static final String APP_PREFERENCES = "mysettings";//назва файла настройокк
    public static final String APP_PREFERENCES_NAME = "Nickname"; // назва змінної
    private List<LatLng> userPlaces = new ArrayList<>();
    private List<LatLng> pointPlaces = new ArrayList<>();
    Intent intentMyIntentService;
    private boolean mAlreadyStartedService = false;

    String TAG;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Отримуемо розташування та позицію камери з збереженого стану інстанції.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);//
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        setContentView(R.layout.activity_maps);
        edDistance = (TextView) findViewById(R.id.editTextDistance);
        edDuration = (TextView) findViewById(R.id.editTextDuration);


        // Будуемо GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Будуемо PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Будуемо FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // будуемо карту.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LATITUDE);
                        Double LatitudeDevice = Double.parseDouble(latitude);
                        String longitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LONGITUDE);
                        Double LongitudeDevice = Double.parseDouble(longitude);
                        Double endPoint=1.0000032686051692;

                        Double latPoint=intent.getDoubleExtra("latPoint",0.0);
                        Double lonPoint=intent.getDoubleExtra("lonPoint",0.0);
                        Log.i("Script", "\n Lat : " + latPoint + "\n Lon: " + lonPoint);

                        //Double result=latPoint/LatitudeDevice;

                        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Notification notification = new NotificationCompat.Builder(getApplicationContext()).setContentTitle("You almost there").setContentText("Well Done").setSmallIcon(R.drawable.images).setSound(uri).build(); // addAction
                        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


                        if(TAG=="on") {
                            Log.i("Script", "ON_ACTIVITY");
                            double pointLatitude = 0;
                            double pointLongitude = 0;
                            for (int i = 0; i < pointPlaces.size(); i++) {
                                pointLatitude = pointPlaces.get(i).latitude;
                                pointLongitude = pointPlaces.get(i).longitude;
                            }
                                if (pointLatitude / LatitudeDevice <= endPoint && pointLongitude / LongitudeDevice <= endPoint)
                                    nm.notify(0, notification);
                            }
                            nm.cancel(0);

                            if (latPoint / LatitudeDevice <= endPoint && lonPoint / LongitudeDevice <= endPoint) {
                                nm.notify(0, notification);
                                Log.i("Script", "ON_SERVICE");
                            }
                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );


    }

    /**
     * Зберігаємо стан карти, коли діяльність призупинена.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    class OnMapClickListenner implements GoogleMap.OnMapClickListener {

        @Override
        public void onMapClick(LatLng latLng) {
            Log.i("Script", "MARKER");
            MarkerOptions markerOptions = new MarkerOptions();//marker create
            markerOptions.position(latLng);//position for the marker
            markerOptions.title(latLng.latitude + ":" + latLng.longitude).toString();//title for the marker
            mMap.clear();//clear previosly touched position
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));//animating to the touched position
            mMap.addMarker(markerOptions);//placing a marker on the touched position
            pointPlaces.add(new LatLng(latLng.latitude, latLng.longitude));
            startStep2();
            getDirections();
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }



    /**
     * Маніпулюємо картою, коли вона доступна.
     * Цей зворотний виклик запускається, коли карта готова до використання.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMapClickListener(new OnMapClickListenner());

        // запит на дозвіл у користувача.
        getLocationPermission();

        // Ввімкнути My Location слой і звязаний з ним елемент керування на карті.
        updateLocationUI();

        // Отримати поточне розташування пристрою та встановити положення карти
        getDeviceLocation();
    }

    /**
     * Отримати поточне розташування пристрою та встановити положення карти
     */
    private void getDeviceLocation() {
        Log.i("Script", "CLICKED_CAMERA_AND_GET_POSITION");
        /*
         * Отримуем краще та найновіше місце розташування пристрою, яке може бути нульовим у рідкісних випадках, коли місцезнаходження недоступне.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Встановлюемо позицію камери на карті у поточному розташуванні пристрою.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            Toast toast = Toast.makeText(getApplicationContext(), "LastLatitude=" + mLastKnownLocation.getLatitude() + "LastLongitude=" + mLastKnownLocation.getLongitude(), Toast.LENGTH_LONG);
                            toast.show();

                            userPlaces.add(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));////////////////////////////////////
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            Toast toast = Toast.makeText(getApplicationContext(), "DefaultLatitude=" + mDefaultLocation.latitude + "DefaultLongitude=" + mDefaultLocation.longitude, Toast.LENGTH_LONG);
                            toast.show();

                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Запитуєм користувача на отримання дозволу на використання місцезнаходження пристрою.
     */
    private void getLocationPermission() {
        /*
         * Запит на місцезнаходження, щоб ми могли отримати місце розташування
         * пристрою. Результат запиту на дозвіл обробляється зворотним callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * результат запиту на отримання дозволів на місцезнаходження.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    /**
     * Відображає форму, яка дозволяє користувачеві вибрати місце зі списку можливих місць.
     */
    private void openPlacesDialog() {
        // Просимо користувача вибрати місце, де він зараз.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Аргумент "який" містить позицію обраного елемента.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Додайте маркер для вибраного місця, в інформаційному вікні, де буде показано інформацію про це місце.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Розташовуемо камеру карти в місці розташування маркера.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

    }

    /**
     * Оновлюємо настройки інтерфейсу карти на основі того, чи користувач надав дозвіл на місцеположення.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    public void getDirections() {

        double userLatitude = 0;
        double userLongitude = 0;
        for (int i = 0; i < userPlaces.size(); i++) {
            userLatitude = userPlaces.get(i).latitude;
            userLongitude = userPlaces.get(i).longitude;
        }
        double pointLatitude = 0;
        double pointLongitude = 0;
        for (int i = 0; i < pointPlaces.size(); i++) {
            pointLatitude = pointPlaces.get(i).latitude;
            pointLongitude = pointPlaces.get(i).longitude;
        }
        String url = "https://maps.googleapis.com/maps/";

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        MapsRetrofit service = retrofit.create(MapsRetrofit.class);

        Call<Maps> call = service.getDistanceDuration("metric", pointLatitude + "," + pointLongitude, userLatitude + "," + userLongitude, "walking");
        call.enqueue(new Callback<Maps>() {
            @Override
            public void onResponse(Call<Maps> call, Response<Maps> response) {

                try {
                    if (line != null) {
                        line.remove();
                    }
                    for (int i = 0; i < response.body().routes.size(); i++) {
                        String encodedString = response.body().routes.get(0).overviewPolyline.points;
                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(20)
                                .color(Color.BLUE)
                                .geodesic(true));
                        Integer distance = response.body().routes.get(i).legs.get(i).distance.value;
                        String time = response.body().routes.get(i).legs.get(i).duration.text;
                        edDistance.setText(distance.toString() + "meters");
                        edDuration.setText(time);
                    }

                } catch (Exception e) {
                    Log.i("Script", "error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Maps> call, Throwable t) {
                Log.i("Script", t.toString());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startStep1();
        TAG="on";
    }


    /**
     *  Перевыряем Google Play services
     */
    private void startStep1() {
        //Перевіряем, чи встановлено цей користувач Google Play сервіс, який використовується оновленнями місцезнаходження.
        if (isGooglePlayServicesAvailable()) {

        } else {
            Toast.makeText(getApplicationContext(), "no_google_playservice_available", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Стартуем Service
     */
    private void startStep2() {
        if (!mAlreadyStartedService) {
            Log.i("Script","SERVICE_STARTED");
            double pointLatitude = 0.0;
            double pointLongitude = 0.0;
            for (int i = 0; i < pointPlaces.size(); i++) {
                pointLatitude = pointPlaces.get(i).latitude;
                pointLongitude = pointPlaces.get(i).longitude;
            }

            // виконуемо явний виклик служби
        startService(new Intent(MapsActivity.this, LocationMonitoringService.class).putExtra("latPoint",pointLatitude).putExtra("lonPoint",pointLongitude));

            mAlreadyStartedService = true;
            //кінець................................................
        }
    }

    /**
     * повертаемо наявність GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        //зупиняемо service
        stopService(new Intent(this, LocationMonitoringService.class));
        mAlreadyStartedService = false;
        super.onDestroy();

    }


}


