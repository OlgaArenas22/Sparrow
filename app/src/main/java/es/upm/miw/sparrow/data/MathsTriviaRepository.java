// MathTriviaRepository.java
package es.upm.miw.sparrow.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.sparrow.data.remote.RetrofitClient;
import es.upm.miw.sparrow.data.remote.otdb.ApiOtdbResponse;
import es.upm.miw.sparrow.data.remote.otdb.OtdbMapper;
import es.upm.miw.sparrow.domain.Question;
import es.upm.miw.sparrow.data.remote.otdb.OpenTriviaApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MathsTriviaRepository {

    private final OpenTriviaApi api = RetrofitClient.otdb();

    public interface QuestionsCallback {
        void onSuccess(List<Question> questions);
        void onError(Throwable t);
    }

    public void fetch(int amount, String difficulty, QuestionsCallback cb) {
        api.getMathQuestions(amount, 19, difficulty, "multiple")
                .enqueue(new Callback<ApiOtdbResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiOtdbResponse> call,
                                           @NonNull Response<ApiOtdbResponse> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            cb.onError(new RuntimeException("HTTP " + resp.code()));
                            return;
                        }
                        List<QuestionDTO> dtos = OtdbMapper.toDTOs(resp.body());
                        if (dtos.isEmpty()) {
                            cb.onError(new IllegalStateException("Sin preguntas de la API"));
                            return;
                        }
                        List<Question> list = new ArrayList<>();
                        for (QuestionDTO dto : dtos) list.add(Question.fromDTO(dto));
                        cb.onSuccess(list);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiOtdbResponse> call, @NonNull Throwable t) {
                        cb.onError(t);
                    }
                });
    }
}
