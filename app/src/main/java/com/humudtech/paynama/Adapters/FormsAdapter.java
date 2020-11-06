package com.humudtech.paynama.Adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.humudtech.paynama.Models.Form;
import com.humudtech.paynama.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FormsAdapter extends
        RecyclerView.Adapter<FormsAdapter.HorizontalViewHolder> {

    private Context mContext;
    private List<Form> mArrayList;

    public FormsAdapter(Context mContext, List<Form> mArrayList) {
        this.mContext = mContext;
        this.mArrayList = mArrayList;
    }

    @Override
    public HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.form_item, parent, false);
        return new HorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HorizontalViewHolder holder, int position) {
        final Form current = mArrayList.get(position);
        holder.title.setText(current.getTitle());

        Glide.with(mContext)
                .load(current.getIcon())
                .placeholder(new ColorDrawable(Color.GRAY))
                .into(holder.icon);
        holder.download.setOnClickListener(v -> {
            String url = current.getFile();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Downloading File");
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            request.setTitle(fileName);
            request.setVisibleInDownloadsUi(true);
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(current.getExtension());
            request.setMimeType(type);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            // in order for this if to run, you must use the android 3.2 to compile your app
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            // get download service and enqueue file
            DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request); //crash here
            Toast.makeText(mContext, "Downloading File...", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    class HorizontalViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.form_title)
        TextView title;
        @BindView(R.id.form_icon)
        ImageView icon;
        @BindView(R.id.bt_download_form)
        ImageButton download;

        public HorizontalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
