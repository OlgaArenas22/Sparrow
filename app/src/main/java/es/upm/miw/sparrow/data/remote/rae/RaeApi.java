package es.upm.miw.sparrow.data.remote.rae;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RaeApi {
    @GET("random")
    Call<RaeRandomResponse> randomWord();
}
