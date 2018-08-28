package ua.genesis.sasha.breadcrumbs;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsRetrofit {
    @GET("api/directions/json?key=AIzaSyDXcQZgtetT19m1niEXGg0J_iIjKU1m3cY")
    Call<Maps> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);
}
