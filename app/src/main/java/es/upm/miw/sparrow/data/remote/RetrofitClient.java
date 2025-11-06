package es.upm.miw.sparrow.data.remote;

import es.upm.miw.sparrow.data.remote.mymemory.MyMemoryApi;
import es.upm.miw.sparrow.data.remote.otdb.OpenTriviaApi;
import es.upm.miw.sparrow.data.remote.rae.RaeApi;
import es.upm.miw.sparrow.data.remote.randomword.RandomWordApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static volatile OpenTriviaApi OTDB;
    private static volatile RandomWordApi RANDOM_WORD;
    private static volatile MyMemoryApi MY_MEMORY;
    private static volatile RaeApi RAE;

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

    public static RandomWordApi randomWord() {
        if (RANDOM_WORD == null) {
            synchronized (RetrofitClient.class) {
                if (RANDOM_WORD == null) {
                    Retrofit r = new Retrofit.Builder()
                            .baseUrl("https://random-word-api.vercel.app/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RANDOM_WORD = r.create(RandomWordApi.class);
                }
            }
        }
        return RANDOM_WORD;
    }

    public static MyMemoryApi myMemory() {
        if (MY_MEMORY == null) {
            synchronized (RetrofitClient.class) {
                if (MY_MEMORY == null) {
                    Retrofit r = new Retrofit.Builder()
                            .baseUrl("https://api.mymemory.translated.net/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    MY_MEMORY = r.create(MyMemoryApi.class);
                }
            }
        }
        return MY_MEMORY;
    }
    public static RaeApi rae() {
        if (RAE == null) {
            synchronized (RetrofitClient.class) {
                if (RAE == null) {
                    Retrofit r = new Retrofit.Builder()
                            .baseUrl("https://rae-api.com/api/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RAE = r.create(RaeApi.class);
                }
            }
        }
        return RAE;
    }
}

