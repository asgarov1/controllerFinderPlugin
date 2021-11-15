package com.javidasgarov.finder.util;

import lombok.experimental.UtilityClass;

import java.util.List;

import static com.javidasgarov.finder.util.TextUtil.matches;

@UtilityClass
public class UrlUtil {
    public static boolean isAMatch(List<String> controllerUrls, String searchUrl) {
        return controllerUrls.stream().anyMatch(url -> matches(searchUrl, url));
    }

}
