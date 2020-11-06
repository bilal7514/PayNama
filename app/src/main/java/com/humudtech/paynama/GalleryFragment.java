package com.humudtech.paynama;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.humudtech.paynama.Adapters.GalleryAdapter;

import java.util.List;

public class GalleryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        ((BaseActivity) getActivity()).hideBanner();
        BigImageViewer.initialize(GlideImageLoader.with(getActivity().getApplicationContext()));

        List<String> images = getArguments().getStringArrayList("images");
        RecyclerView recycler = root.findViewById(R.id.recycler);
        recycler.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recycler.setAdapter(new GalleryAdapter(images));

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recycler);
        return root;
    }
}
