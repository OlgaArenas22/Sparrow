package es.upm.miw.sparrow.data.remote.otdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiOtdbResponse {
    @SerializedName("response_code")
    public int responseCode;

    @SerializedName("results")
    public List<ApiOtdbQuestion> results;
}
