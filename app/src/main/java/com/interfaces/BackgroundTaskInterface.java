package com.interfaces;

/**
 * Created by Avishek on 6/19/2015.
 */
public interface BackgroundTaskInterface {

    public void onStarted();
    public void onCompleted(String jsonStr);

}
