package es.upm.miw.sparrow.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.miw.sparrow.data.QuestionDTO;

public class Question {
    public String question;
    public List<String> answers;
    public int correctIndex;
    public String image;

    public static Question fromDTO(QuestionDTO dto) {
        Question q = new Question();
        q.question= dto.question;
        q.image = dto.image;

        List<String> opts = new ArrayList<>();
        opts.add(dto.correct);
        opts.add(dto.wrong1);
        opts.add(dto.wrong2);
        opts.add(dto.wrong3);

        String correct = dto.correct;
        Collections.shuffle(opts);
        q.answers = opts;
        q.correctIndex = opts.indexOf(correct);
        return q;
    }
}
