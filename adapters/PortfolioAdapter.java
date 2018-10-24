package com.aconst.spinareg.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.aconst.spinareg.profile.PortfolioItem;

import java.util.ArrayList;
import java.util.List;

public class ProfilePortfolioAdapter extends RecyclerView.Adapter<SessionListAdapter.ViewHolderSession> {
    private List<PortfolioItem> portfolioItems = new ArrayList<>();

    public ProfilePortfolioAdapter(List<PortfolioItem> portfolioItems) {
        this.portfolioItems = portfolioItems;
    }

    @Override
    public SessionListAdapter.ViewHolderSession onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(SessionListAdapter.ViewHolderSession holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
