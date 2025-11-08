package es.upm.miw.sparrow.ui.fragments;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;

import es.upm.miw.sparrow.ui.audio.MusicManager;

public abstract class BaseQuizFragment extends Fragment {

    protected BaseQuizFragment(@LayoutRes int layoutRes) {
        super(layoutRes);
    }

    /** Cada quiz define su pista (coloca los mp3/ogg en res/raw). */
    @RawRes protected abstract int musicRes();

    @Override public void onResume() {
        super.onResume();
        MusicManager.get(requireContext()).startIfEnabled(musicRes());
    }

    @Override public void onPause() {
        super.onPause();
        // Pausamos al salir de la pantalla del quiz
        MusicManager.get(requireContext()).pause();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        // Seguridad extra: liberar si el fragment se destruye
        MusicManager.get(requireContext()).stopAndRelease();
    }
}
