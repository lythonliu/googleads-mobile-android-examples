package com.google.example.gms.fsnexample;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * ViewHolder instance which holds the actual feed
 */
public abstract class FeedViewHolder extends RecyclerView.ViewHolder {
    public FeedViewHolder(View v) {
        super(v);
    }

    public void attach() {
        // Subclass should override this
    }

    public void detach() {
        // Subclass should override this
    }
}


