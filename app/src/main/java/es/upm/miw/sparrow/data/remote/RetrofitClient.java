package es.upm.miw.sparrow.data.remote;

import es.upm.miw.sparrow.data.remote.otdb.OpenTriviaApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static volatile OpenTriviaApi OTDB;

    public static OpenTriviaApi otdb() {
        if (OTDB == null) {
            synchronized (RetrofitClient.class) {
                if (OTDB == null) {
                    Retrofit r = new Retrofit.Builder()
                            .baseUrl("https://opentdb.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    OTDB = r.create(OpenTriviaApi.class);
                }
            }
        }
        return OTDB;
    }
}
