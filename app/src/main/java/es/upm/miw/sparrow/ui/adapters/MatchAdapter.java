package es.upm.miw.sparrow.ui.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.upm.miw.sparrow.R;
import es.upm.miw.sparrow.domain.Match;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.VH> {

    private final List<Match> data = new ArrayList<>();
    private final Context context;

    public MatchAdapter(@NonNull Context context, List<Match> initial) {
        this.context = context;
        if (initial != null) data.addAll(initial);
    }

    /** Actualiza los datos del adapter y refresca la lista */
    public void submit(List<Match> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Match m = data.get(pos);

        // Rellenar textos
        h.tvCategory.setText(m.category);
        h.tvPoints.setText(m.pointsPretty());
        h.tvDate.setText(formatDate(m.timestamp));

        // Colores dinámicos según puntuación
        int bgColor = context.getColor(m.isPassed() ? R.color.greenAnswer : R.color.redAnswer);
        int fgColor = context.getColor(android.R.color.white);

        h.card.setCardBackgroundColor(bgColor);
        h.tvCategory.setTextColor(fgColor);
        h.tvPoints.setTextColor(fgColor);
        h.tvDate.setTextColor(fgColor);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvCategory;
        final TextView tvPoints;
        final TextView tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    /** Formatea la fecha del timestamp a "dd/MM/yyyy HH:mm" */
    private String formatDate(Timestamp ts) {
        Date d = (ts != null) ? ts.toDate() : new Date();
        return DateFormat.format("dd/MM/yyyy HH:mm", d).toString();
    }
}
