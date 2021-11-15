package com.javidasgarov.finder.util;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ComparatorUtil {

    /**
     * Done so that better match Urls come up first
     *
     * @param urls
     * @param searchUrl
     * @return
     */
    public static int comparingUrls(List<String> urls, String searchUrl) {
        if (urls.stream()
                .map(ComparatorUtil::getLastElementOfUrl)
                .anyMatch(value -> value.equals(getLastElementOfUrl(searchUrl)))) {
            return 1;
        } else return -1;
    }

    private static String getLastElementOfUrl(String value) {
        String[] elements = value.split("/");
        return elements[elements.length - 1];
    }
}
