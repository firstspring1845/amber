package net.firsp.amber.filter;

import twitter4j.Status;

public class NopFilter implements StatusFilter {

    public static final NopFilter INSTANCE = new NopFilter();

    @Override
    public boolean filter(Status status) {
        return false;
    }
}
