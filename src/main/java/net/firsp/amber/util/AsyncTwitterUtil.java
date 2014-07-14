package net.firsp.amber.util;

import android.app.Activity;

import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by owner on 2014/07/06.
 */
public class AsyncTwitterUtil {

    public static TwitterListener getTwitterListener(final Activity activity) {
        return new TwitterAdapter() {
            @Override
            public void updatedProfile(User user) {
                CroutonUtil.showText(activity, "プロフィールを変更しましたv('ω')");
            }

            @Override
            public void updatedProfileImage(User user) {
                CroutonUtil.showText(activity, "アイコンを変更しましたv('ω')");
            }

            @Override
            public void retweetedStatus(Status retweetedStatus) {
                CroutonUtil.showText(activity, "リツイートしましたv('ω')");
            }

            @Override
            public void createdFavorite(Status status) {
                CroutonUtil.showText(activity, "ふぁぼふぁぼしましたv('ω')");
            }

            @Override
            public void destroyedFavorite(Status status) {
                CroutonUtil.showText(activity, "あんふぁぼしましたv('ω')");
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                CroutonUtil.error(activity);
            }
        };
    }
}
