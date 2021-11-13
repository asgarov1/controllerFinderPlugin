package com.javidasgarov.finder.util;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.strip;

public class TextUtil {

    /**
     * This method allows for better comparison as without it
     * searching for an endpoint "shop" in an annotation with value "/shop"
     * would fail
     *
     * @param searchUrl target url
     * @param url       one of the values in the annotation
     * @return whether annotation's url matches
     */
    public static boolean containsIgnoringFirstAndLastSlashes(String searchUrl, String url) {
        return strip(searchUrl, "/").contains(strip(url, "/"));
    }

    public static List<String> getAnnotationValues(String value) {
        if (StringUtils.isEmpty(value)) {
            return emptyList();
        }
        String[] values = value.replace("\"", "")
                .replace("\"", "")
                .replace("{", "")
                .replace("}", "")
                .split(",");

        return Arrays.stream(values)
                .map(String::trim)
                .collect(toList());
    }

    public static List<String> appendPrefixToAllValues(String prefix, List<String> values) {
        return values.stream()
                .map(value -> append(prefix, value))
                .collect(Collectors.toList());
    }

    @NotNull
    private static String append(String prefix, String value) {
        if (value.length() > 0 && value.indexOf("/") != 0) {
            value = "/" + value;
        }
        return (prefix + value).replace("//", "/");
    }
}
