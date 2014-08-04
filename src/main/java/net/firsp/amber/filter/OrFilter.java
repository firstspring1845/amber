package net.firsp.amber.filter;

import twitter4j.Status;

public class OrFilter implements StatusFilter {

    StatusFilter lhs;
    StatusFilter rhs;

    private OrFilter() {
    }

    public static OrFilter of(StatusFilter lhs, StatusFilter rhs) {
        OrFilter filter = new OrFilter();
        filter.lhs = lhs;
        filter.rhs = rhs;
        return filter;
    }

    @Override
    public boolean filter(Status status) {
        return lhs.filter(status) | rhs.filter(status);
    }
}
