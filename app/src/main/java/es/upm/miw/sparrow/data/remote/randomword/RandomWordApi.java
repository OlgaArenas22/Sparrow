package es.upm.miw.sparrow.data.remote.randomword;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RandomWordApi {
    @GET("api")
    Call<List<String>> getWords(@Query("words") int words);
}
