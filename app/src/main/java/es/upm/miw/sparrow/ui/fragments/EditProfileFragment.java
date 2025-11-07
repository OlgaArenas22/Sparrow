package es.upm.miw.sparrow.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.local.AvatarPrefs;
import es.upm.miw.sparrow.data.local.AvatarUrlBuilder;
import es.upm.miw.sparrow.ui.fragments.sheet.AvatarPickerBottomSheet;

public class EditProfileFragment extends Fragment {

    private static final String ARG_EMAIL = "arg_email";
    public static final String FR_KEY = "avatar_pick_key";
    public static final String FR_BUNDLE_SEED = "seed";

    private String email;
    private ImageButton btnAvatar, btnReturn;
    private ImageView ivAvatar;
    private TextView tvEmail;

    public static EditProfileFragment newInstance(String email) {
        EditProfileFragment f = new EditProfileFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EMAIL, email);
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        } else {
            SharedPreferences sp = requireContext()
                    .getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            email = sp.getString("email", null);
        }

        getParentFragmentManager().setFragmentResultListener(FR_KEY, this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        String seed = result.getString(FR_BUNDLE_SEED);
                        if (seed != null) {
                            AvatarPrefs.saveSeed(requireContext(), email, seed);
                            refreshAvatarUi();
                            refreshNavHeaderInActivity();
                        }
                    }
                });
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tvEmail = v.findViewById(R.id.tvEmail);
        btnAvatar = v.findViewById(R.id.btnAvatar);
        ivAvatar = v.findViewById(R.id.ivAvatar);
        btnReturn = v.findViewById(R.id.btnReturn);

        tvEmail.setText(email != null ? email : getString(R.string.emailHint));
        refreshAvatarUi();

        btnAvatar.setOnClickListener(view -> {
            AvatarPickerBottomSheet sheet = new AvatarPickerBottomSheet();
            sheet.show(getParentFragmentManager(), "avatar_picker");
        });

        btnReturn.setOnClickListener(view ->{
            getParentFragmentManager().popBackStack();
        });
    }

    private void refreshAvatarUi() {
        String seed = AvatarPrefs.getSeed(requireContext(), email);
        String url = AvatarUrlBuilder.buildAutoBg(seed, 256);
        Glide.with(this).load(url).placeholder(R.drawable.ic_sparrow_rounded).into(ivAvatar);
    }

    private void refreshNavHeaderInActivity() {
        if (getActivity() instanceof es.upm.miw.sparrow.ui.activities.HomeActivity) {
            ((es.upm.miw.sparrow.ui.activities.HomeActivity) getActivity())
                    .refreshHeaderAvatar();
        }
    }
}
