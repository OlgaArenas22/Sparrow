package es.upm.miw.sparrow.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import es.upm.miw.sparrow.R;

public class ResultsDialog extends DialogFragment {

    public static final String TAG = "GameResultsDialog";
    private static final String ARG_POINTS = "points";
    private static final String ARG_TOTAL = "total";

    public static ResultsDialog newInstance(int points, int total) {
        ResultsDialog dialog = new ResultsDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_POINTS, points);
        args.putInt(ARG_TOTAL, total);
        dialog.setArguments(args);
        return dialog;
    }

    public interface GameResultsDialogListener {
        void onReturnToMenuClicked();
        void onPlayAgainClicked();
    }

    private GameResultsDialogListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment parent = getParentFragment();
        if (parent instanceof ResultsDialog.GameResultsDialogListener) {
            listener = (ResultsDialog.GameResultsDialogListener) parent;
        } else if (getActivity() instanceof ResultsDialog.GameResultsDialogListener) {
            listener = (ResultsDialog.GameResultsDialogListener) getActivity();
        } else {
            throw new IllegalStateException("Parent Fragment or Activity must implement GameResultsDialogListener");
        }

        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_music, null);

        TextView tvPoints = view.findViewById(R.id.points);
        ImageView ivZazu = view.findViewById(R.id.imageView);
        ImageButton btnMenu = view.findViewById(R.id.btnReturn);
        ImageButton btnAgain = view.findViewById(R.id.btnGameAgain);

        Bundle args = getArguments();
        int numPoints = args != null ? args.getInt(ARG_POINTS) : 0;
        int totalPoints = args != null ? args.getInt(ARG_TOTAL) : 0;

        tvPoints.setText(String.format("%d/%d", numPoints, totalPoints));

        if (numPoints >= (totalPoints / 2)) {
            ivZazu.setImageResource(R.drawable.happy_zazu);
        } else {
            ivZazu.setImageResource(R.drawable.serious_zazu);
        }

        btnMenu.setOnClickListener(v -> {
            if (listener != null) listener.onReturnToMenuClicked();
            dismiss();
        });

        btnAgain.setOnClickListener(v -> {
            if (listener != null) listener.onPlayAgainClicked();
            dismiss();
        });

        Dialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.TransparentDialog)
                .setView(view)
                .setCancelable(false)
                .create();

        return dialog;
    }

}