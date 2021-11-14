package com.javidasgarov.finder.util;

import java.util.List;

import static com.javidasgarov.finder.util.TextUtil.matches;

public class UrlUtil {
    public static boolean isAMatch(List<String> controllerUrls, String searchUrl) {
        return controllerUrls.stream().anyMatch(url -> matches(searchUrl, url));
    }

}
