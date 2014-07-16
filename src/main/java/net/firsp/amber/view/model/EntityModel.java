package net.firsp.amber.view.model;

import twitter4j.MediaEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class EntityModel {

    String str;
    String url;

    public EntityModel(Object entity) {
        if (entity instanceof UserMentionEntity) {
            UserMentionEntity userMentionEntity = (UserMentionEntity) entity;
            StringBuilder sb = new StringBuilder();
            sb.append("@");
            sb.append(userMentionEntity.getScreenName());
            str = sb.toString();
            sb = new StringBuilder();
            sb.append("https://twitter.com/");
            sb.append(userMentionEntity.getScreenName());
            url = sb.toString();
        } else if (entity instanceof URLEntity) {
            URLEntity urlEntity = (URLEntity) entity;
            str = urlEntity.getExpandedURL();
            url = str;
        } else if (entity instanceof MediaEntity) {
            MediaEntity mediaEntity = (MediaEntity) entity;
            str = mediaEntity.getMediaURL();
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
