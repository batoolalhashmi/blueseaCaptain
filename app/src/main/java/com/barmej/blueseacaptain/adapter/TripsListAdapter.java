package com.barmej.blueseacaptain.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.domain.entity.Trip;

import java.util.List;

public class TripsListAdapter extends RecyclerView.Adapter<TripsListAdapter.TripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private OnTripClickListener mTripClickListener;
    private List<Trip> mTripsList;

    public TripsListAdapter(List<Trip> tripsList, OnTripClickListener onTripClickListener) {
        this.mTripsList = tripsList;
        this.mTripClickListener = onTripClickListener;
    }

    @NonNull
    @Override
    public TripsListAdapter.TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripsListAdapter.TripViewHolder holder, int position) {
        holder.bind(mTripsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mTripsList.size();
    }

    public class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripPickUpPortTextView;
        TextView tripDestinationPortTextView;
        TextView tripDateTextView;
        TextView tripAvailableSeats;
        TextView tripBookedUpSeats;
        Trip trip;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripPickUpPortTextView = itemView.findViewById(R.id.pick_up_port_text_view);
            tripDestinationPortTextView = itemView.findViewById(R.id.destination_port_text_view);
            tripDateTextView = itemView.findViewById(R.id.date);
            tripAvailableSeats = itemView.findViewById(R.id.available_seats);
            tripBookedUpSeats = itemView.findViewById(R.id.booked_up_seats);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mTripClickListener.onTripClick(trip);
                }
            });
        }

        public void bind(Trip trip) {
            this.trip = trip;
            tripPickUpPortTextView.setText(trip.getPickUpPort());
            tripDestinationPortTextView.setText(trip.getDestinationPort());
            tripDateTextView.setText(trip.getFormattedDate());
            tripAvailableSeats.setText(trip.getAvailableSeats());
            tripBookedUpSeats.setText(trip.getBookedUpSeats());
        }
    }
}
