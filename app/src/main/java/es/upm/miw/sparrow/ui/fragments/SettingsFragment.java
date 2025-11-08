package es.upm.miw.sparrow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.ui.audio.MusicManager;

public class SettingsFragment extends Fragment {

    private SwitchMaterial swMusic;
    private Slider sliderVolume;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        swMusic = v.findViewById(R.id.swMusic);
        sliderVolume = v.findViewById(R.id.sliderVolume);

        final MusicManager mm = MusicManager.get(requireContext());

        boolean enabled = mm.isEnabled();
        swMusic.setChecked(enabled);
        if (sliderVolume != null) {
            sliderVolume.setValue(mm.getVolume() * 100f);
            sliderVolume.setEnabled(enabled);
        }
        sliderVolume.setEnabled(swMusic.isChecked());

        swMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            sliderVolume.setEnabled(isChecked);
        });

        swMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            mm.setEnabled(isChecked);
            if (!isChecked) {
                mm.stopAndRelease();
            }
            if (sliderVolume != null) sliderVolume.setEnabled(isChecked);
        });

        if (sliderVolume != null) {
            sliderVolume.addOnChangeListener((slider, value, fromUser) -> {
                float vol01 = value / 100f;
                mm.setVolume(vol01);
            });
        }
    }
}
