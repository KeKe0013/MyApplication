package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfilePictureAdapter extends RecyclerView.Adapter<ProfilePictureAdapter.ViewHolder> {
    private final List<Integer> pictureIds;
    private final Context context;
    private int selectedPosition = -1;
    private OnItemClickListener listener;
    private RecyclerView recyclerView;

    public ProfilePictureAdapter(Context context, List<Integer> pictureIds) {
        this.context = context;
        this.pictureIds = pictureIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        int size = dpToPx(); // 默認大小
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        imageView.setLayoutParams(params);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int actualPosition = position % pictureIds.size();
        int pictureId = pictureIds.get(actualPosition);
        @SuppressLint("DiscouragedApi") int pictureResourceId = context.getResources().getIdentifier("picture" + pictureId, "drawable", context.getPackageName());

        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(pictureResourceId);
        Drawable circleDrawable = ((user) context).circle(drawable);
        holder.imageView.setImageDrawable(circleDrawable);

        holder.imageView.setOnClickListener(v -> {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldSelectedPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onItemClick(pictureId);
            }
            recyclerView.smoothScrollToPosition(position);
        });
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // 無限循環
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private int dpToPx() {
        return Math.round(100 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int pictureId);
    }
}