package com.javidasgarov.finder.util;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang.StringUtils.*;

public class TextUtil {

    private static final List<String> IRRELEVANT_CHARACTERS = List.of("\"", ",", " ", "{", "}");
    public static final String PLACEHOLDER_REGEX = "[A-z0-9]*";
    public static final String URL_BEGINNING_REGEX = "([A-z0-9:/._-]*\\/)?";
    public static final String PLACEHOLDER = "\\{[A-z0-9]*}";

    /**
     * This method allows for better comparison as without stripping slashes
     * "shop" wouldn't match "shop/" or "/shop/"
     *
     * It also replaces all placeholders (e.g. '{booking_id}') with a regex and then performs
     * a regex pattern match
     *
     * @param searchUrl target url
     * @param url       one of the values in the annotation
     * @return whether annotation's url matches
     */
    public static boolean matches(String searchUrl, String url) {
        searchUrl = strip(searchUrl, "/");
        String urlPattern = strip(url, "/")
                .replace("/","\\/")
                .replaceAll(PLACEHOLDER, PLACEHOLDER_REGEX);
        return Pattern.matches(URL_BEGINNING_REGEX + urlPattern, searchUrl);
    }

    public static List<String> getAnnotationValues(PsiElement[] values) {
        return Arrays.stream(values)
                .map(PsiElement::getText)
                .filter(not(IRRELEVANT_CHARACTERS::contains))
                .map(value -> strip(value, "\""))
                .collect(Collectors.toList());
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
