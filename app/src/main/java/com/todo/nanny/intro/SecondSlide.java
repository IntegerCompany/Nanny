package com.todo.nanny.intro;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.todo.nanny.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondSlide extends Fragment {


    public SecondSlide() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_slide, container, false);
    }


}
