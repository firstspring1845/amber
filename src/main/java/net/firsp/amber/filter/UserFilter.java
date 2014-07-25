package net.firsp.amber.filter;

import java.util.Arrays;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class UserFilter implements StatusFilter {

    long[] follows = new long[0];

    public UserFilter(long[] follow) {
        follows = Arrays.copyOf(follow, follow.length);
        Arrays.sort(follows);
    }

    @Override
    public boolean filter(Status status) {
        if (!contains(status.getUser().getId())) {
            return false;
        }
        if (status.isRetweet()) {
            return true;
        }
        for (UserMentionEntity ume : status.getUserMentionEntities()) {
            if (!contains(ume.getId())) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(long id) {
        return Arrays.binarySearch(follows, id) >= 0;
    }

}
