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
import com.example.projectdroid.models.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);


        if (holder.textTitle != null) {
            holder.textTitle.setText(book.getTitle());
        }


        if (holder.textAuthor != null) {
            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                holder.textAuthor.setText(book.getAuthors().get(0));
            } else {
                holder.textAuthor.setText("Sem Autor");
            }
        }


        if (holder.imageCover != null) {
            if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(book.getCoverUrl())
                        .into(holder.imageCover);
            } else {
                holder.imageCover.setImageDrawable(null); // Limpa se não houver imagem
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle;
        public TextView textAuthor;
        public ImageView imageCover;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            // ATENÇÃO: Confirma se estes IDs ('textTitle', 'textAuthor', 'imageCover')
            // batem certo com os IDs que tens no teu ficheiro de layout 'item_book.xml'
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            imageCover = itemView.findViewById(R.id.imageCover);
        }
    }
}