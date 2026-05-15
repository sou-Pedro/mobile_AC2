package com.facens.mobile_ac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LivrosAdapter extends RecyclerView.Adapter<LivrosAdapter.ViewHolder> {
    private List<Livros> livros;

    public interface OnItemClickListener {
        void onItemClick(Livros livro);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Livros livro);
    }

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public LivrosAdapter(List<Livros> livros) {
        this.livros = livros;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Livros l = livros.get(pos);
        holder.txt1.setText(l.getTitulo());
        holder.txt2.setText("Autor: " + l.getAutor() + " (" + l.getAno() + ") - " + l.getGenero());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(l);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(l);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return livros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt1, txt2;
        public ViewHolder(View itemView) {
            super(itemView);
            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
