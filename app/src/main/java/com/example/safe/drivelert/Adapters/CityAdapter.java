package com.example.safe.drivelert.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe.drivelert.R;
import com.example.safe.drivelert.Utility.Const;
import com.example.safe.drivelert.Utility.TinyDB;
import com.example.safe.drivelert.Utility.Utils;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.viewHolder> {

    ArrayList<String> arrayList;
    Context context;
    TinyDB tinyDB;

    public CityAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
        tinyDB = new TinyDB(context);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city , parent , false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, final int position) {
            String city = arrayList.get(position);
            holder.name.setText(city);

            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    tinyDB.putBoolean(
                            Utils.calculateHash(arrayList.get(position)),
                            false);

                    removeAt(position);
                    tinyDB.putListString(Const.CITIES_LIST , arrayList);

                }
            });
    }

    public void removeAt(int position) {
        arrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, arrayList.size());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static  class viewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        Button remove;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_cityNameAdapter);
            remove = itemView.findViewById(R.id.btn_removeAdapter);
        }
    }
}
