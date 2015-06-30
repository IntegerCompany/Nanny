package com.todo.nanny.delegates;

import android.app.Activity;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.todo.nanny.listeners.OnConnectionListener;

/**
 * Created by dmytro on 6/29/15.
 */
public class ServerDelegate implements OnConnectionListener {


    @Override
    public void connected(Activity activity) {

        if (activity != null) Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void received(Activity activity, Connection connection, Object o) {
        if (activity != null) Toast.makeText(activity, "Received", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void disconnected(Activity activity) {
        if (activity != null) Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
    }
}
