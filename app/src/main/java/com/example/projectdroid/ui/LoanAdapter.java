package com.example.projectdroid.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectdroid.R;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.utils.DateUtils;

import java.util.List;

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {

    private final List<Loan> loanList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Loan loan);
    }

    public LoanAdapter(List<Loan> loanList) {
        this.loanList = loanList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        Loan loan = loanList.get(position);

        holder.textTitle.setText(loan.getTitle());
        holder.textAuthor.setText(loan.getAuthors());

        if (loan.isActive()) {
            String text = "Devolver até " + loan.getDueDate();
            if (DateUtils.isBeforeToday(loan.getDueDate())) {
                text += " (em atraso)";
            }
            holder.textDueDate.setText(text);
        } else {
            holder.textDueDate.setText("Devolvido em " + loan.getReturnDate());
        }

        if (loan.getCoverUrl() != null && !loan.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(loan.getCoverUrl()).into(holder.imageCover);
        } else {
            holder.imageCover.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(loan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return loanList.size();
    }

    public static class LoanViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle;
        public TextView textAuthor;
        public TextView textDueDate;
        public ImageView imageCover;

        public LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.loanTextTitle);
            textAuthor = itemView.findViewById(R.id.loanTextAuthor);
            textDueDate = itemView.findViewById(R.id.loanTextDueDate);
            imageCover = itemView.findViewById(R.id.loanImageCover);
        }
    }
}
