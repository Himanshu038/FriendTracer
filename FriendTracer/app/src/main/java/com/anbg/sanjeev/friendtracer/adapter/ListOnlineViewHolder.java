package com.anbg.sanjeev.friendtracer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anbg.sanjeev.friendtracer.R;
import com.anbg.sanjeev.friendtracer.interfaces.ItemClickListenener;

/**
 * Created by BEST BUY on 09-11-2017.
 */

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView emailText;
    public ItemClickListenener itemClickListenener;
    public ListOnlineViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        emailText = (TextView)itemView.findViewById(R.id.email);
    }

    public void setItemClickListenener(ItemClickListenener itemClickListenener) {
        this.itemClickListenener = itemClickListenener;
    }

    @Override
    public void onClick(View view) {
        itemClickListenener.onClick(view,getAdapterPosition());
    }
}
