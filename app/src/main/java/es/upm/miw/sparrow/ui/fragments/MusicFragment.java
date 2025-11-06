package es.upm.miw.sparrow.ui.fragments;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.domain.Question;
import es.upm.miw.sparrow.ui.dialogs.ResultsDialog;
import es.upm.miw.sparrow.view.MusicViewModel;

public class MusicFragment extends Fragment implements ResultsDialog.GameResultsDialogListener{

    private static final int MILLIS = 10 * 1000;

    private MusicViewModel vm;

    private TextView tvQuestion;
    private ImageView image;
    private ProgressBar timer;
    private ProgressBar loadingSpinner;
    private View quizContentContainer;
    private MaterialButton btnA, btnB, btnC, btnD;

    private ColorStateList origBgTintA, origBgTintB, origBgTintC, origBgTintD;
    private Integer origTextA, origTextB, origTextC, origTextD;

    private ValueAnimator timerAnimator;

    public MusicFragment() {}

    public static MusicFragment newInstance() { return new MusicFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vm = new ViewModelProvider(this).get(MusicViewModel.class);

        loadingSpinner = view.findViewById(R.id.loading_spinner);
        quizContentContainer = view.findViewById(R.id.quiz_content_container);
        image = view.findViewById(R.id.image);
        timer = view.findViewById(R.id.timer);
        tvQuestion = view.findViewById(R.id.question);
        btnA = view.findViewById(R.id.answer1);
        btnB = view.findViewById(R.id.answer2);
        btnC = view.findViewById(R.id.answer3);
        btnD = view.findViewById(R.id.answer4);

        timer.setMax(MILLIS);
        timer.setProgress(MILLIS);

        origBgTintA = btnA.getBackgroundTintList();
        origBgTintB = btnB.getBackgroundTintList();
        origBgTintC = btnC.getBackgroundTintList();
        origBgTintD = btnD.getBackgroundTintList();
        origTextA = btnA.getCurrentTextColor();
        origTextB = btnB.getCurrentTextColor();
        origTextC = btnC.getCurrentTextColor();
        origTextD = btnD.getCurrentTextColor();

        btnA.setOnClickListener(v -> vm.answer(0));
        btnB.setOnClickListener(v -> vm.answer(1));
        btnC.setOnClickListener(v -> vm.answer(2));
        btnD.setOnClickListener(v -> vm.answer(3));

        vm.getCurrentIndex().observe(getViewLifecycleOwner(), idx -> {
            resetAndStartTimerBar();
            renderQuestion();
        });

        vm.getQuestions().observe(getViewLifecycleOwner(), qs -> {
            if (qs != null && !qs.isEmpty()) {
                showQuizContent();
                renderQuestion();
            }
        });

        vm.getSelectedIndex().observe(getViewLifecycleOwner(), si -> renderColors());
        vm.getCorrectIndex().observe(getViewLifecycleOwner(), ci -> renderColors());
        vm.getLocked().observe(getViewLifecycleOwner(), locked -> {
            setButtonsEnabled(!locked);
            if (locked != null && locked) stopTimerBar();
            renderColors();
        });

        vm.getFinished().observe(getViewLifecycleOwner(), fin -> {
            if (fin != null && fin) {
                showEndGameDialog();
            }
        });

        vm.getError().observe(getViewLifecycleOwner(), exception -> {
            if (exception != null && isAdded()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage(exception.getMessage())
                        .setCancelable(false)
                        .setPositiveButton(R.string.txtReturn, (dialog, which) ->
                                getParentFragmentManager().popBackStack())
                        .show();
            }
        });

        vm.loadQuestionsIfNeeded();
    }

    public void onReturnToMenuClicked() {
        setButtonsEnabled(false);
        getParentFragmentManager().popBackStack();
    }

    public void onPlayAgainClicked() {
        setButtonsEnabled(false);
        getParentFragmentManager().popBackStack();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, MusicFragment.newInstance())
                    .addToBackStack("music")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    private void showEndGameDialog() {
        if (!isAdded()) return;
        if (getChildFragmentManager().findFragmentByTag(ResultsDialog.TAG) != null) return;
        requireView().post(() -> {
            if (!isAdded()) return;
            ResultsDialog dialog = ResultsDialog.newInstance(vm.getPoints(), vm.getTotalQuestions());
            dialog.show(getChildFragmentManager(), ResultsDialog.TAG);
        });
    }

    private void showQuizContent() {
        if (loadingSpinner != null && quizContentContainer != null) {
            loadingSpinner.setVisibility(View.GONE);
            quizContentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void renderQuestion() {
        Question q = vm.getCurrentQuestionSync();
        if (q == null) return;

        tvQuestion.setText(q.question);

        if (q.answers != null && q.answers.size() == 4) {
            btnA.setText(q.answers.get(0));
            btnB.setText(q.answers.get(1));
            btnC.setText(q.answers.get(2));
            btnD.setText(q.answers.get(3));
        }

        if (q.image != null) {
            int resId = getResources().getIdentifier(q.image, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                image.setImageResource(resId);
            } else {
                image.setImageResource(R.drawable.quiz_image);
            }
        } else {
            image.setImageResource(R.drawable.quiz_image);
        }
        restoreOriginalStyles();
        setButtonsEnabled(true);
    }

    private void renderColors() {
        Boolean isLocked = vm.getLocked().getValue();
        if (isLocked == null || !isLocked) {
            return;
        }

        Integer selected = vm.getSelectedIndex().getValue();
        Integer correct = vm.getCorrectIndex().getValue();

        paintNeutral(btnA);
        paintNeutral(btnB);
        paintNeutral(btnC);
        paintNeutral(btnD);

        if (correct != null) {
            paintCorrect(getButton(correct));
        }
        if (selected != null && (correct == null || !selected.equals(correct))) {
            paintWrong(getButton(selected));
        }
    }

    private void resetAndStartTimerBar() {
        stopTimerBar();
        timer.setMax(MILLIS);
        timer.setProgress(MILLIS);

        timerAnimator = ValueAnimator.ofInt(MILLIS, 0);
        timerAnimator.setDuration(MILLIS);
        timerAnimator.addUpdateListener(anim -> {
            int remaining = (int) anim.getAnimatedValue();
            timer.setProgress(remaining);
        });
        timerAnimator.start();
    }

    private void stopTimerBar() {
        if (timerAnimator != null) {
            timerAnimator.cancel();
            timerAnimator = null;
        }
    }

    private void restoreOriginalStyles() {
        btnA.setBackgroundTintList(origBgTintA);
        btnB.setBackgroundTintList(origBgTintB);
        btnC.setBackgroundTintList(origBgTintC);
        btnD.setBackgroundTintList(origBgTintD);

        if (origTextA != null) btnA.setTextColor(origTextA);
        if (origTextB != null) btnB.setTextColor(origTextB);
        if (origTextC != null) btnC.setTextColor(origTextC);
        if (origTextD != null) btnD.setTextColor(origTextD);
    }

    private void paintNeutral(MaterialButton b) {
        int white = ContextCompat.getColor(requireContext(), android.R.color.white);
        int black = ContextCompat.getColor(requireContext(), android.R.color.black);

        b.setBackgroundTintList(ColorStateList.valueOf(white));
        b.setTextColor(black);
    }

    private void paintCorrect(MaterialButton b) {
        int green = ContextCompat.getColor(requireContext(), R.color.greenAnswer);
        b.setBackgroundTintList(ColorStateList.valueOf(green));
        b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    private void paintWrong(MaterialButton b) {
        int red = ContextCompat.getColor(requireContext(), R.color.redAnswer);
        b.setBackgroundTintList(ColorStateList.valueOf(red));
        b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    private MaterialButton getButton(int idx) {
        switch (idx) {
            case 0: return btnA;
            case 1: return btnB;
            case 2: return btnC;
            default: return btnD;
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
        btnD.setEnabled(enabled);
    }

    @Override
    public void onDestroyView() {
        stopTimerBar();
        super.onDestroyView();
    }
}