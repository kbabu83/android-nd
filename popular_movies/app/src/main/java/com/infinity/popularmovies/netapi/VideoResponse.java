package com.infinity.popularmovies.netapi;

/**
 * Created by KBabu on 21-Dec-15.
 */
public class VideoResponse {
    private int id;
    private VideoItem[] results;

    public class VideoItem {
        private String id;
        private String name;
        private String key;
        private String site;
        private String type;

        public VideoItem(String id, String name, String key, String site, String type) {
            this.id = id;
            this.name = name;
            this.key = key;
            this.site = site;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }

        public String getSite() {
            return site;
        }

        public String getType() {
            return type;
        }
    }

    public VideoResponse(int id, VideoItem[] results) {
        this.id = id;
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public VideoItem[] getResults() {
        return results;
    }

}
