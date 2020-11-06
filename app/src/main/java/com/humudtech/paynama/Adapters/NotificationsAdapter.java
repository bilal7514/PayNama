package com.humudtech.paynama.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.humudtech.paynama.Models.Notification;
import com.humudtech.paynama.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private Context mContext;
    private List<Notification> mArrayList;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public NotificationsAdapter(Context mContext,
                        List<Notification> mArrayList) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_grid, parent, false);
            return new HorizontalViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HorizontalViewHolder) {
            populateItemRows((HorizontalViewHolder) holder, position);
        } else if (holder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) holder, position);
        }

    }
    private void populateItemRows(HorizontalViewHolder holder, int position) {

        final Notification current = mArrayList.get(position);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("images",current.getImages());
        holder.Title.setText(current.getTitle());
        holder.IssueDate.setText(current.getIssueDate());
        holder.Reference.setText(current.getReference());
        if(!current.getThumbnail().equals("")){
            Glide.with(mContext)
                    .load(current.getThumbnail())
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .into(holder.Thumbnail);
        }
        holder.linearLayout.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.galleryFragment,bundle);
        });
        holder.Thumbnail.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.galleryFragment,bundle);
        });

    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return mArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    class HorizontalViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.lyt_parent)
        LinearLayout linearLayout;
        @BindView(R.id.title)
        TextView Title;
        @BindView(R.id.thumbnail)
        ImageView Thumbnail;
        @BindView(R.id.date)
        TextView IssueDate;
        @BindView(R.id.reference)
        TextView Reference;

        public HorizontalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {

    }
}
