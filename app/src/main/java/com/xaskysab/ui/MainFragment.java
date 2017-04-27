package com.xaskysab.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xaskysab.gan.TMode;
import com.xaskysab.gan.TjGeny;
import com.xaskysab.gan.TypeActionGeny;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by XaskYSab on 2017/4/25 0025.
 */

@TypeActionGeny
public class MainFragment extends Fragment {

    View root;
    private Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment, container, false);

        button = (Button) root.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        asyncThread();
                    }
                }).start();



            }
        });




        return root;
    }


    @TjGeny(TMode.MAIN)
    public void updateView() {

        button.setText("main");
    }

    @TjGeny(TMode.Async)
    public void asyncThread() {

        Log.e("tag","asyncThread");

        updateView();
    }
}
