package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.arch.lifecycle.ViewModel;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.places.Place;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;

import java.util.List;
import java.util.logging.Logger;

public class SavedDateIdeasAdapter extends RecyclerView.Adapter<SavedDateIdeasAdapter.ViewHolder> {
    private List<DateIdea> myDataset;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnail;
        TextView addressField, phoneNumberField, descriptionField, attributionField;
        ImageButton deleteButton;
        ToggleButton favoriteButton;

        public ViewHolder(View v) {
            super(v);
            deleteButton = v.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(this);
            favoriteButton = v.findViewById(R.id.favorite_button);
            favoriteButton.setOnClickListener(this);
            thumbnail = v.findViewById(R.id.date_card_thumbnail);
            addressField = v.findViewById(R.id.address_card_text);
            phoneNumberField = v.findViewById(R.id.phone_number_card_text);
            descriptionField = v.findViewById(R.id.date_card_label);
            attributionField = v.findViewById(R.id.attribution_card_field);
            attributionField.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.delete_button:
                    remove(getAdapterPosition());
                    break;
                case R.id.favorite_button:
                    toggleFavorite(getAdapterPosition());
                    break;
            }
        }
    }

    public SavedDateIdeasAdapter(List<DateIdea> dataset) {
        myDataset = dataset;
    }

    @Override
    public SavedDateIdeasAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.date_idea_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DateIdea dateIdea = myDataset.get(position);
        holder.addressField.setText(dateIdea.getActivity());
        dateIdea.getPlace(new DateIdea.CompletionHandler() {
            @Override
            public void handleCompletion(Object created) {
                Place place = (Place) created;
                holder.phoneNumberField.setText(place.getPhoneNumber());
                holder.addressField.setText(place.getAddress());
                holder.descriptionField.setText(dateIdea.getActivity() + " at " + place.getName());
            }
        });
        dateIdea.getPhotoFromOffset(0, new DateIdea.CompletionHandler() {
            @Override
            public void handleCompletion(Object created) {
                PlacePhoto photo = (PlacePhoto) created;
                holder.thumbnail.setImageBitmap(photo.getBitmap());
                holder.attributionField.setText(Html.fromHtml(photo.getAttribution().toString()));
            }
        });
        dateIdea.favorited(new DateIdea.CompletionHandler() {
            @Override
            public void handleCompletion(Object created) {
                boolean favorited = (boolean) created;
                holder.favoriteButton.setChecked(favorited);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myDataset.size();
    }

    private void toggleFavorite(int position){
        DateIdea dateIdea = myDataset.get(position);
        dateIdea.toggleFavorite();
        notifyDataSetChanged();
    }

    private void remove(int position) {
        if(position >=0 && position < myDataset.size()) {
            DateIdea dateIdea = myDataset.remove(position);
            dateIdea.delete();
            notifyItemRemoved(position);
        }
    }

}
