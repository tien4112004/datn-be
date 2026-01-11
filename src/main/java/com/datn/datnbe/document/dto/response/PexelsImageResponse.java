package com.datn.datnbe.document.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PexelsImageResponse {
    private Integer page;

    @JsonProperty("per_page")
    private Integer perPage;

    private List<PexelsPhoto> photos;

    @JsonProperty("next_page")
    private String nextPage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PexelsPhoto {
        private Long id;
        private Integer width;
        private Integer height;
        private String url;
        private String photographer;

        @JsonProperty("photographer_url")
        private String photographerUrl;

        @JsonProperty("photographer_id")
        private Long photographerId;

        @JsonProperty("avg_color")
        private String avgColor;

        private PexelsSrc src;
        private Boolean liked;
        private String alt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PexelsSrc {
        private String original;
        private String large2x;
        private String large;
        private String medium;
        private String small;
        private String portrait;
        private String landscape;
        private String tiny;
    }
}
