package com.lbtmo.ticketingsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class TicketViolationAdapter extends ArrayAdapter<TicketViolation> {
    private Context context;
    private List<TicketViolation> violations;

    public TicketViolationAdapter(Context context, List<TicketViolation> violations) {
        super(context, 0, violations);
        this.context = context;
        this.violations = violations;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_dropdown_item, parent, false);
        }

        TicketViolation violation = getItem(position);

        TextView codeTextView = convertView.findViewById(R.id.textViolationCode);
        TextView titleTextView = convertView.findViewById(R.id.textViolationTitle);

        if (violation != null) {
            codeTextView.setText(violation.getCode());
            titleTextView.setText(violation.getTitle());
        }

        return convertView;
    }
}
