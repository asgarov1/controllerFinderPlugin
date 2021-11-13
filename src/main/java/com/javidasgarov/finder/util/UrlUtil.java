package com.javidasgarov.finder.util;

import java.util.List;

import static com.javidasgarov.finder.util.TextUtil.containsIgnoringFirstAndLastSlashes;

public class UrlUtil {
    public static boolean containsMatch(List<String> controllerUrls, String searchUrl) {
        return controllerUrls.stream()
                .anyMatch(url -> containsIgnoringFirstAndLastSlashes(searchUrl, url));
    }

}
