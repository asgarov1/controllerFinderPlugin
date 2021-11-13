package com.javidasgarov.finder.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilTest {

    @Test
    @DisplayName("should return empty list if text is empty or null")
    void getValuesForEmptyString() {
        assertEquals(List.of(), TextUtil.getAnnotationValues(null));
        assertEquals(List.of(), TextUtil.getAnnotationValues(""));
    }

    @Test
    @DisplayName("should return a list with one value if text is contains only one value")
    void getValuesForOneValue() {
        String url = "home";
        assertEquals(List.of(url), TextUtil.getAnnotationValues("\"" + url + "\""));

        url = "/home";
        assertEquals(List.of(url), TextUtil.getAnnotationValues("\"" + url + "\""));
    }

    @Test
    @DisplayName("should return a list of all values if text contains an array of values")
    void getValuesForSeveralValues() {
        String urlOne = "home";
        String urlTwo = "/shop";
        String values = "{\"" + urlOne + "\", \"" + urlTwo + "\"}";
        assertEquals(List.of(urlOne, urlTwo), TextUtil.getAnnotationValues(values));
    }
}