package com.sandy_rock_studios.macbookair.randomdateideagenerator.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class OutOfIdeasFragment extends Fragment implements View.OnClickListener{
    private View myView;
    private OnFragmentInteractionListener myListener;

    public OutOfIdeasFragment() {
    }

    public static OutOfIdeasFragment newInstance() {
        OutOfIdeasFragment fragment = new OutOfIdeasFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_out_of_ideas, container, false);
        myView.findViewById(R.id.refresh_ideas_button).setOnClickListener(this);
        return myView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refresh_ideas_button:
                myListener.onOutOfIdeasInteraction();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OutOfIdeasFragment.OnFragmentInteractionListener) {
            myListener = (OutOfIdeasFragment.OnFragmentInteractionListener) context;
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
        void onOutOfIdeasInteraction();
    }
}
