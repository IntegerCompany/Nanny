package com.todo.nanny.simpleobject;

/**
 * Created by Andriy on 30.06.2015.
 */
public class MessageSO {

public static final int LET_ME_HEAR_BABY = 1, READY_FOR_RECEIVING_VOICE = 2, START_SERVER_RECORDER = 3;

    private int code;

    public MessageSO(int code){
        this.code = code;
    }
    public MessageSO(){

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
