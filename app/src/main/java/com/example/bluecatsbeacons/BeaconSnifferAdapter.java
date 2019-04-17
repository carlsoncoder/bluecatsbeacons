package com.example.bluecatsbeacons;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bluecats.sdk.BCBeacon;
import java.util.List;
import com.example.bluecatsbeacons.databinding.ItemBeaconSnifferBinding;

public class BeaconSnifferAdapter extends RecyclerView.Adapter<BeaconSnifferAdapter.ViewHolder> {

    public static final int[] ROW_COLORS = new int[]{ Color.WHITE, Color.parseColor( "#EEEEEE" ) };
    private final List<BCBeacon> beacons;

    public BeaconSnifferAdapter(final List<BCBeacon> allBeacons) {
        this.beacons = allBeacons;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(ItemBeaconSnifferBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final BCBeacon beacon = this.beacons.get(position);
        holder.binding.setBeacon(beacon);

        final StringBuilder categories = new StringBuilder();
        for (int i = 0; i < beacon.getCategories().length; i++) {
            if(i > 0) {
                categories.append(", ");
            }

            categories.append(beacon.getCategories()[i].getName());
        }

        holder.binding.txtCategories.setText(categories.toString());

        int colourPos = position % BeaconSnifferAdapter.ROW_COLORS.length;
        holder.binding.getRoot().setBackgroundColor( BeaconSnifferAdapter.ROW_COLORS[colourPos] );
    }

    @Override
    public int getItemCount()
    {
        return this.beacons == null ? 0 : this.beacons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemBeaconSnifferBinding binding;

        public ViewHolder(final ItemBeaconSnifferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public BCBeacon getBeacon() {
            return this.binding.getBeacon();
        }
    }

}