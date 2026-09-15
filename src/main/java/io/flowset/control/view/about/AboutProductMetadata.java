package io.flowset.control.view.about;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AboutProductMetadata {
    private List<ExternalLink> externalLinks = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    @Getter
    @Setter
    public static class ExternalLink {
        private String url;
        private String label;
        private String icon;
        private Boolean iconLink;
    }

    @Getter
    @Setter
    public static class Product {
        private String name;
        private String url;
        private String description;
        private Boolean released = Boolean.FALSE;
    }
}
