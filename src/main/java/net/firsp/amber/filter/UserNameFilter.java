package net.firsp.amber.filter;

import twitter4j.Status;

public class UserNameFilter implements StatusFilter {

    String sn;

    private UserNameFilter() {
    }

    public static UserNameFilter of(String sn) {
        UserNameFilter filter = new UserNameFilter();
        filter.sn = sn;
        return filter;
    }

    @Override
    public boolean filter(Status status) {
        if (status.getUser().getScreenName().equals(sn)) {
            return true;
        }
        return false;
    }
}
