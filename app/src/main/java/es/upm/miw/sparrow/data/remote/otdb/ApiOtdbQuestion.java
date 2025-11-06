package es.upm.miw.sparrow.data.remote.otdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiOtdbQuestion {
    @SerializedName("question")
    public String question;
    @SerializedName("correct_answer")
    public String correctAnswer;
    @SerializedName("incorrect_answers")
    public List<String> incorrectAnswers;
}
