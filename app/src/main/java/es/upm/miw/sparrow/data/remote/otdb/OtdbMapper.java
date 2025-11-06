package es.upm.miw.sparrow.data.remote.otdb;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.sparrow.common.HtmlUtils;
import es.upm.miw.sparrow.data.QuestionDTO;

public final class OtdbMapper {
    private OtdbMapper(){}

    public static List<QuestionDTO> toDTOs(ApiOtdbResponse resp){
        List<QuestionDTO> out = new ArrayList<>();
        if (resp == null || resp.results == null) return out;

        for (ApiOtdbQuestion q : resp.results) {
            if (q == null || q.incorrectAnswers == null || q.incorrectAnswers.size() < 3) continue;

            QuestionDTO dto = new QuestionDTO();
            dto.question = HtmlUtils.decode(q.question);
            dto.correct  = HtmlUtils.decode(q.correctAnswer);
            dto.wrong1   = HtmlUtils.decode(q.incorrectAnswers.get(0));
            dto.wrong2   = HtmlUtils.decode(q.incorrectAnswers.get(1));
            dto.wrong3   = HtmlUtils.decode(q.incorrectAnswers.get(2));
            dto.image    = null;

            out.add(dto);
        }
        return out;
    }
}
