package es.upm.miw.sparrow.data.datasource;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.sparrow.data.QuestionDTO;
import es.upm.miw.sparrow.data.QuestionsDataSource;
import es.upm.miw.sparrow.data.QuestionsRepository;
import es.upm.miw.sparrow.data.remote.RetrofitClient;
import es.upm.miw.sparrow.data.remote.otdb.ApiOtdbResponse;
import es.upm.miw.sparrow.data.remote.otdb.OtdbMapper;
import es.upm.miw.sparrow.data.remote.otdb.OpenTriviaApi;
import es.upm.miw.sparrow.domain.Question;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenTdbDataSource implements QuestionsDataSource {

    private final OpenTriviaApi api = RetrofitClient.otdb();

    @Override
    public void fetch(int amount, @Nullable String difficulty, QuestionsRepository.Callback cb) {
        api.getMathQuestions(amount, 19, difficulty, "multiple")
                .enqueue(new Callback<ApiOtdbResponse>() {
                    @Override public void onResponse(Call<ApiOtdbResponse> call, Response<ApiOtdbResponse> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            cb.onError(new RuntimeException("HTTP " + resp.code()));
                            return;
                        }
                        List<QuestionDTO> dtos = OtdbMapper.toDTOs(resp.body());
                        if (dtos.isEmpty()) {
                            cb.onError(new IllegalStateException("Sin preguntas de OpenTDB"));
                            return;
                        }
                        List<Question> out = new ArrayList<>();
                        for (QuestionDTO dto : dtos) out.add(Question.fromDTO(dto));
                        cb.onSuccess(out);
                    }
                    @Override public void onFailure(Call<ApiOtdbResponse> call, Throwable t) {
                        cb.onError(t);
                    }
                });
    }
}
