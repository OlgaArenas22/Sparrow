package es.upm.miw.sparrow.data.remote.mymemory;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyMemoryApi {
    @GET("get")
    Call<MyMemoryResponse> translate(@Query("q") String text,
                                     @Query("langpair") String langPair);
}
