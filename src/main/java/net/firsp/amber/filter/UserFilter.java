package net.firsp.amber.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class UserFilter implements StatusFilter {

    Set<Long> follows = Collections.EMPTY_SET;

    public UserFilter(Collection<Long> follow) {
        follows = new HashSet<Long>(follow);
    }

    @Override
    public boolean filter(Status status) {
        if (!follows.contains(status.getUser().getId())) {
            return false;
        }
        if (status.isRetweet()) {
            return true;
        }
        for (UserMentionEntity ume : status.getUserMentionEntities()) {
            if (!follows.contains(ume.getId())) {
                return false;
            }
        }
        return true;
    }

}
