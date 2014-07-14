package net.firsp.amber.account;

import android.content.Context;

import java.io.File;
import java.io.Serializable;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Account implements Serializable {

    private String ck;
    private String cs;
    private String at;
    private String ats;

    private long id;
    private String sn;

    public Account(String ck, String cs, String at, String ats, long id, String sn) {
        this.ck = ck;
        this.cs = cs;
        this.at = at;
        this.ats = ats;
        this.id = id;
        this.sn = sn;
    }

    public File getAccountDir(Context c) {
        return new File(c.getCacheDir(), String.valueOf(getId()));
    }

    public long getId() {
        return id;
    }

    public String getScreenName() {
        return sn;
    }

    public Twitter getTwitter() {
        Configuration c = new ConfigurationBuilder()
                .setOAuthConsumerKey(ck)
                .setOAuthConsumerSecret(cs)
                .setOAuthAccessToken(at)
                .setOAuthAccessTokenSecret(ats)
                .build();
        return new TwitterFactory(c).getInstance();
    }

    public AsyncTwitter getAsyncTwitter() {
        return new AsyncTwitterFactory(getTwitter().getConfiguration()).getInstance();
    }

    public TwitterStream getTwitterStream() {
        return new TwitterStreamFactory(getTwitter().getConfiguration()).getInstance();
    }
}
