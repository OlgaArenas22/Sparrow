package es.upm.miw.sparrow.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import es.upm.miw.sparrow.R;

public class ExitQuizDialog extends DialogFragment {

    public static final String TAG = "ExitQuizDialog";
    private ExitQuizDialogListener listener;

    public static ExitQuizDialog newInstance() {
        return new ExitQuizDialog();
    }

    public interface ExitQuizDialogListener {
        void onReturnToMenuClicked();
        void onResumeQuizClicked();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment parent = getParentFragment();
        if (parent instanceof ExitQuizDialogListener) {
            listener = (ExitQuizDialogListener) parent;
        } else if (getActivity() instanceof ExitQuizDialogListener) {
            listener = (ExitQuizDialogListener) getActivity();
        } else {
            throw new IllegalStateException("Parent Fragment or Activity must implement ExitQuizDialogListener");
        }
        setCancelable(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_exit_quiz, null);

        Button btnContinue = view.findViewById(R.id.btnResume);
        Button btnExit = view.findViewById(R.id.btnExit);


        btnContinue.setOnClickListener(v -> {
            if (listener != null) listener.onResumeQuizClicked();
            dismiss();
        });

        btnExit.setOnClickListener(v -> {
            if (listener != null) listener.onReturnToMenuClicked();
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(true)
                .create();
    }
}
