package es.upm.miw.sparrow.ui.fragments.sheet;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.data.datasource.FunEmojiRemoteDataSource;
import es.upm.miw.sparrow.data.remote.dicebear.AvatarResponse;
import es.upm.miw.sparrow.ui.fragments.EditProfileFragment;

public class AvatarPickerBottomSheet extends BottomSheetDialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            dialog.setOnShowListener(di -> {
                BottomSheetDialog d = (BottomSheetDialog) di;
                View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                    behavior.setDraggable(true);
                    // Si usas layout_height="match_parent" en el RV, esto ayuda:
                    // behavior.setPeekHeight(bottomSheet.getHeight());
                }
            });
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_avatar_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = v.findViewById(R.id.rvAvatars);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        List<AvatarResponse> items = new FunEmojiRemoteDataSource().list(32);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rv.setHasFixedSize(true);

        rv.setAdapter(new AvatarAdapter(items, seed -> {
            Bundle b = new Bundle();
            b.putString(EditProfileFragment.FR_BUNDLE_SEED, seed);
            getParentFragmentManager().setFragmentResult(EditProfileFragment.FR_KEY, b);
            dismiss();
        }));
    }

    interface OnPick { void pick(String seed); }

    static class AvatarAdapter extends RecyclerView.Adapter<VH> {
        private final List<AvatarResponse> data;
        private final OnPick onPick;

        AvatarAdapter(List<AvatarResponse> data, OnPick onPick) {
            this.data = data; this.onPick = onPick;
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_avatar, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            AvatarResponse it = data.get(pos);
            Glide.with(h.img.getContext()).load(it.getUrl()).into(h.img);
            h.itemView.setOnClickListener(v -> onPick.pick(it.getSeed()));
        }
        @Override public int getItemCount() { return data.size(); }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
