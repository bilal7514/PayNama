package com.humudtech.paynama.Adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.humudtech.paynama.Models.Book;
import com.humudtech.paynama.Models.Form;
import com.humudtech.paynama.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BooksAdapter extends
        RecyclerView.Adapter<BooksAdapter.HorizontalViewHolder> {

    private Context mContext;
    private List<Book> mArrayList;

    public BooksAdapter(Context mContext, List<Book> mArrayList) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
    }

    @Override
    public BooksAdapter.HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.book_item, parent, false);
        return new BooksAdapter.HorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BooksAdapter.HorizontalViewHolder holder, int position) {
        final Book current = mArrayList.get(position);
        holder.title.setText(current.getTitle());
        Glide.with(mContext).load(current.getThumbnail()).placeholder(new ColorDrawable(Color.GRAY)).into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("Book",current.getFile());
            Navigation.findNavController(v).navigate(R.id.readBookFragment,bundle);
        });
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    class HorizontalViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.image)
        ImageView thumbnail;

        public HorizontalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
