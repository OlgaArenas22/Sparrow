// OpenTriviaApi.java
package es.upm.miw.sparrow.data.remote.otdb;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenTriviaApi {
    @GET("api.php")
    Call<ApiOtdbResponse> getMathQuestions(
            @Query("amount") int amount,
            @Query("category") int category,
            @Query("difficulty") String difficulty,
            @Query("type") String type
    );
}
