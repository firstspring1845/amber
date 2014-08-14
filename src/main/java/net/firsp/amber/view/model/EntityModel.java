package net.firsp.amber.view.model;

import twitter4j.MediaEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class EntityModel {

    String str;
    String url;

    public EntityModel(Object entity) {
        if (entity instanceof User || entity instanceof UserMentionEntity) {
            String sn;
            if (entity instanceof User) {
                sn = ((User) entity).getScreenName();
            } else {
                sn = ((UserMentionEntity) entity).getScreenName();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("@");
            sb.append(sn);
            str = sb.toString();
            sb = new StringBuilder();
            sb.append("https://twitter.com/");
            sb.append(sn);
            url = sb.toString();
        } else if (entity instanceof MediaEntity) {
            MediaEntity mediaEntity = (MediaEntity) entity;
            str = mediaEntity.getMediaURL();
            url = str;
        } else if (entity instanceof URLEntity) {
            URLEntity urlEntity = (URLEntity) entity;
            str = urlEntity.getExpandedURL();
            url = str;
        } else {
            str = ":(";
            url = str;
        }
    }

    public String getContentUrl() {
        return url;
    }

    public String toString() {
        return str;
    }

}
