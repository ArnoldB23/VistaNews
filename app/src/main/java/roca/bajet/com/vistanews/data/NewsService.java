package roca.bajet.com.vistanews.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Arnold on 8/22/2017.
 */

public interface NewsService {

    //business, entertainment, gaming, general, music, politics, science-and-nature, sport, technology
    public static final String CAT_BUSINESS = "business";
    public static final String CAT_ENTERTAINMENT = "entertainment";
    public static final String CAT_GAMING = "gaming";
    public static final String CAT_GENERAL = "general";
    public static final String CAT_MUSIC = "music";
    public static final String CAT_POLITICS = "politics";
    public static final String CAT_SCIENCE = "science-and-nature";
    public static final String CAT_SPORTS = "sport";
    public static final String CAT_TECH = "technology";

    @GET("v1/sources")
    Call<GetSourceResponse> getSource(
        @Query("language") String language,
        @Query("country") String country,
        @Query("category") String category
    );

    @GET("v1/sources")
    Call<GetSourceResponse> getSource(
            @Query("language") String language,
            @Query("country") String country
    );

    @GET("v1/sources")
    Call<GetSourceResponse> getSource(
            @Query("category") String category
    );

    @GET("v1/sources")
    Call<GetSourceResponse> getSource();
}
