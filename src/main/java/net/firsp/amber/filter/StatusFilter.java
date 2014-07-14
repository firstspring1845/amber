package net.firsp.amber.filter;

import twitter4j.Status;

public interface StatusFilter {

    //trueだと反映
    public boolean filter(Status status);
}
