package com.infinity.popularmovies.netapi;

/**
 * Created by KBabu on 18-Dec-15.
 */
public class ConfigResponse {
    private ImageConfig images;

    public class ImageConfig {
        private String base_url;
        private String secure_base_url;
        private String[] poster_sizes;

        public ImageConfig(String base_url, String secure_base_url, String[] poster_sizes) {
            this.base_url = base_url;
            this.secure_base_url = secure_base_url;
            this.poster_sizes = poster_sizes;
        }

    }

    public ConfigResponse(ImageConfig images) {
        this.images = images;
    }

    public String getBaseUrl() {
        return images.base_url;
    }

    public String getSecureBaseUrl() {
        return images.secure_base_url;
    }

    public String[] getPosterSizes() {
        return images.poster_sizes;
    }

}

