package es.upm.miw.sparrow.ui.fragments;

import androidx.annotation.LayoutRes;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;

import es.upm.miw.sparrow.ui.audio.MusicManager;

public abstract class BaseQuizFragment extends Fragment {

    protected BaseQuizFragment(@LayoutRes int layoutRes) {
        super(layoutRes);
    }

    @RawRes protected abstract int musicRes();

    @Override public void onResume() {
        super.onResume();
        MusicManager.get(requireContext()).startIfEnabled(musicRes());
    }

    @Override public void onPause() {
        super.onPause();
        MusicManager.get(requireContext()).pause();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        MusicManager.get(requireContext()).stopAndRelease();
    }
}
