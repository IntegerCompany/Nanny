package com.todo.nanny.listeners;

import android.app.Activity;

import com.esotericsoftware.kryonet.Connection;

/**
 * Created by dmytro on 6/29/15.
 */
public interface OnConnectionListener {


    void connected(Activity activity);

    void received(Activity activity, Connection connection, Object o);

    void disconnected(Activity activity);

}
