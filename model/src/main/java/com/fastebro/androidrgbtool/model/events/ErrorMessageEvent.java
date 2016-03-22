package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 17/06/15.
 * Project: rgb-tool
 */
public class ErrorMessageEvent {
    public final String message;

    public ErrorMessageEvent(String message) {
        this.message = message;
    }
}
