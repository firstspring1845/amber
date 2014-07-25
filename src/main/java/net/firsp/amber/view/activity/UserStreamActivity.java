package net.firsp.amber.view.activity;

import android.os.Bundle;

import twitter4j.Status;

public class UserStreamActivity extends StreamTimelineActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userStream.user();
        new Thread(()->{
            try{
                for (Status status : account.getTwitter().getHomeTimeline()) {
                    adapter.add(status);
                    adapter.refresh();
                }
            }catch(Exception e){
            }
        }).start();
    }
}
