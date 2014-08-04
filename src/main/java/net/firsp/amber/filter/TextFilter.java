package net.firsp.amber.filter;

import twitter4j.Status;

public class TextFilter implements StatusFilter {

    String text;

    private TextFilter() {
    }

    public static TextFilter of(String text) {
        TextFilter filter = new TextFilter();
        filter.text = text;
        return filter;
    }

    @Override
    public boolean filter(Status status) {
        return status.getText().contains(text);
    }
}
