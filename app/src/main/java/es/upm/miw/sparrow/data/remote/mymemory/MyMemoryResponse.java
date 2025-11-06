package es.upm.miw.sparrow.data.remote.mymemory;

import com.google.gson.annotations.SerializedName;

public class MyMemoryResponse {
    @SerializedName("responseData")
    public Data responseData;

    public static class Data {
        @SerializedName("translatedText")
        public String translatedText;
    }
}
