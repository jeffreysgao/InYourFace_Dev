package com.example.jeffrey_gao.inyourface_dev;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jinnan on 2/25/17.
 *
 * To track the concentration level of an image.
 */

public class AttentionFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.attention_fragment, container, false);
    }
}
