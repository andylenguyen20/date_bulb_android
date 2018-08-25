package com.sandy_rock_studios.macbookair.randomdateideagenerator.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.DataParser;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.DateIdea;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.FirebaseWriter;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.PlacePhoto;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.RequestHandler;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.DateCreationHandler;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.JSONResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DateIdeaFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "DateIdeaFragment";
    private static final String ARG_DATE_IDEA = "DateIdeaParam";

    private DateIdea myDateIdea;
    private OnFragmentInteractionListener myListener;
    private View myView;

    // variable to track event time
    private long myLastClickTime = 0;

    public DateIdeaFragment() {
    }

    public static DateIdeaFragment newInstance(String dateIdea) {
        DateIdeaFragment fragment = new DateIdeaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE_IDEA, dateIdea);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String dateStr = getArguments().getString(ARG_DATE_IDEA);
            myDateIdea = new DateIdea(dateStr, getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_date_idea, container, false);

        myView.findViewById(R.id.saved_button).setOnClickListener(this);
        myView.findViewById(R.id.discarded_button).setOnClickListener(this);
        myView.findViewById(R.id.left_image_button).setOnClickListener(this);
        myView.findViewById(R.id.right_image_button).setOnClickListener(this);

        displayPhoto(0);
        displayDateDescription();
        return myView;
    }

    private void displayDateDescription(){
        final TextView title = myView.findViewById(R.id.date_title);
        final TextView address = myView.findViewById(R.id.address_field);
        myDateIdea.getPlace(new DateIdea.CompletionHandler() {
            @Override
            public void handleCompletion(Object created) {
                Place place = (Place) created;
                title.setText(myDateIdea.getActivity() + " at " + place.getName());
                address.setText(place.getAddress());
                Activity activity = getActivity();
                if(activity != null && activity.findViewById(R.id.progress_bar) != null){
                    activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayPhoto(int offset){
        final ImageView displayImage = myView.findViewById(R.id.specific_place_photo);
        final TextView attributionField = myView.findViewById(R.id.attribution_field);
        attributionField.setMovementMethod(LinkMovementMethod.getInstance());
        myDateIdea.getPhotoFromOffset(offset, new DateIdea.CompletionHandler() {
            @Override
            public void handleCompletion(Object created) {
                PlacePhoto photo = (PlacePhoto) created;
                displayImage.setImageBitmap(photo.getBitmap());
                attributionField.setText(Html.fromHtml(photo.getAttribution().toString()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        // Preventing multiple clicks, using threshold of .5 second
        if (SystemClock.elapsedRealtime() - myLastClickTime < 500) {
            return;
        }
        myLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.discarded_button:
                myDateIdea.delete();
                myListener.onIdeaInteraction();
                break;

            case R.id.saved_button:
                myDateIdea.save();
                myListener.onIdeaInteraction();
                break;

            case R.id.left_image_button:
                displayPhoto(-1);
                break;

            case R.id.right_image_button:
                displayPhoto(1);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            myListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onIdeaInteraction();
    }
}
