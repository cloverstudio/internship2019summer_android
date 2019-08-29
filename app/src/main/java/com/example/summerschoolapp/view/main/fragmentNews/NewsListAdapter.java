package com.example.summerschoolapp.view.main.fragmentNews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.summerschoolapp.R;
import com.example.summerschoolapp.database.entity.NewsArticle;
import com.example.summerschoolapp.model.News;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.CustomVievHolder> {

    private List<NewsArticle> data = new ArrayList<>();
    private NewsListInteraction listener;

    public NewsListAdapter(NewsListAdapter.NewsListInteraction listener) {
        this.listener = listener;
    }

    public void setData(List<NewsArticle> newData) {
        if (newData != null && !newData.isEmpty()) {
            this.data.clear();
            this.data.addAll(newData);
            notifyDataSetChanged();
        }
    }

    //TODO do I need this?
    //ignore
    public void clearList(List<NewsArticle> data) {
        this.data.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsListAdapter.CustomVievHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recycler_row_news_list, parent, false);
        return new NewsListAdapter.CustomVievHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsListAdapter.CustomVievHolder holder, int position) {
        NewsArticle item = data.get(position);

        holder.ivNewsPicture.setImageDrawable(null);
        holder.tvNewsTitle.setText(item.getTitle_log());

//        Timber.d("DAT: " + item.getTitle() + " " + item.getId());

        holder.tvNewsMessage.setText(item.getMessage_log());
        holder.tvNewsAuthor.setText(String.format("%s %s", item.getFirst_name(), item.getLast_name()));

        holder.rowParentLayout.setOnClickListener(view -> {
            if (listener != null) {
                listener.onNewsClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class CustomVievHolder extends RecyclerView.ViewHolder {

        ImageView ivNewsPicture;
        TextView tvNewsTitle;
        TextView tvNewsMessage;
        TextView tvNewsAuthor;
        ConstraintLayout rowParentLayout;

        public CustomVievHolder(@NonNull View itemView) {
            super(itemView);

            ivNewsPicture = itemView.findViewById(R.id.iv_news_picture);
            tvNewsTitle = itemView.findViewById(R.id.tv_news_title);
            tvNewsMessage = itemView.findViewById(R.id.tv_news_message);
            tvNewsAuthor = itemView.findViewById(R.id.tv_news_author);
            rowParentLayout = itemView.findViewById(R.id.row_parent_layout);
        }
    }

    interface NewsListInteraction {
        void onNewsClicked(NewsArticle news);
    }
}