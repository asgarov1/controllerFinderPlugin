package com.javidasgarov.finder.util;

import com.intellij.psi.PsiElement;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang.StringUtils.strip;

@UtilityClass
public class TextUtil {

    private static final List<String> IRRELEVANT_CHARACTERS = List.of("\"", ",", " ", "{", "}" ,")", "(");
    public static final String PLACEHOLDER_REGEX = "[A-z0-9]*";
    public static final String URL_BEGINNING_REGEX = "([A-z0-9:/._-]*/)?";
    public static final String PLACEHOLDER = "\\{[^/]*}";

    /**
     * This method allows for better comparison as without stripping slashes
     * "shop" wouldn't match "shop/" or "/shop/"
     * <p>
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
                .replace("/", "\\/")
                .replaceAll(PLACEHOLDER, PLACEHOLDER_REGEX);
        return Pattern.matches(URL_BEGINNING_REGEX + urlPattern, searchUrl);
    }

    public static List<String> getAnnotationValues(PsiElement[] values) {
        List<String> rawValues = Arrays.stream(values)
                .map(PsiElement::getText)
                .filter(not(IRRELEVANT_CHARACTERS::contains))
                .map(value -> strip(value, "\""))
                .collect(Collectors.toList());

        return reduceIntoActualValues(rawValues);
    }

    public static List<String> reduceIntoActualValues(List<String> rawValues) {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < rawValues.size(); i++) {
            if (!rawValues.get(i).equals("+")) {
                value.append(rawValues.get(i));
            }
            if (i == rawValues.size() - 1 ||
                    !rawValues.get(i).equals("+") && !rawValues.get(i + 1).equals("+")) {
                result.add(value.toString());
                value = new StringBuilder();
            }
        }
        return result;
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
