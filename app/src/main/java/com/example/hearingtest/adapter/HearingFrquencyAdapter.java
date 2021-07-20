package com.example.hearingtest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hearingtest.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HearingFrquencyAdapter extends RecyclerView.Adapter<HearingFrquencyAdapter.VH> {
    public static class VH extends RecyclerView.ViewHolder{
        public final TextView mTvf;

        public VH(@NonNull View itemView) {
            super(itemView);
            mTvf = itemView.findViewById(R.id.tv_f);
        }
    }

    public List<FrequencyBean> mFrequencyList;

    public HearingFrquencyAdapter(List<FrequencyBean> frequencyList) {
        this.mFrequencyList = frequencyList;
    }



    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hearing_frequency_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FrequencyBean item = mFrequencyList.get(position);
        holder.mTvf.setText(String.valueOf(item.getLabel()));
        holder.mTvf.setEnabled(item.isEnable());
    }




    @Override
    public int getItemCount() {
        return mFrequencyList.size();
    }
}
