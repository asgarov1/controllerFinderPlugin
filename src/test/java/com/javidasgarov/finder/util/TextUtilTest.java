package com.javidasgarov.finder.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.javidasgarov.finder.util.TextUtil.matches;
import static org.junit.jupiter.api.Assertions.*;

class TextUtilTest {

    @Test
    @DisplayName("should correctly reduce into endpoints")
    void reduceIntoActualValues() {
        List<String> values = List.of(
                "/bookings/",
                "+",
                "{",
                "+",
                "BOOKING_ID",
                "+",
                "}",
                "/someOtherEndpoint"
        );

        List<String> reducedValues = TextUtil.reduceIntoActualValues(values);
        assertEquals(2, reducedValues.size());
        assertEquals("/bookings/{BOOKING_ID}", reducedValues.get(0));
        assertEquals("/someOtherEndpoint", reducedValues.get(1));
    }

    @Test
    void reduceIntoActualValuesShouldWorkWithJustOneEndpoint() {
        List<String> values = List.of(
                "{",
                "+",
                "BOOKING_ID",
                "+",
                "}"
        );

        List<String> reducedValues = TextUtil.reduceIntoActualValues(values);
        assertEquals(1, reducedValues.size());
        assertEquals("{BOOKING_ID}", reducedValues.get(0));
    }

    @Test
    void reduceIntoActualValuesShouldWorkWithNothingToReduce() {
        List<String> values = List.of(
                "/endpointOne",
                "/endpointTwo"
        );

        List<String> reducedValues = TextUtil.reduceIntoActualValues(values);
        assertEquals(2, reducedValues.size());
        assertEquals(values.get(0), reducedValues.get(0));
        assertEquals(values.get(1), reducedValues.get(1));
    }

    @Test
    void reduceIntoActualValuesShouldWorkWithEmptyList() {
        List<String> reducedValues = TextUtil.reduceIntoActualValues(List.of());
        assertTrue(reducedValues.isEmpty());
    }

    // match method

    @Test
    void matchShouldWorkCorrectly() {
        String searchUrlOne = "https://api.alpega.pw/sb-ui-dev/api/templateSnapshots/108231";
        String falseUrl = "/templateSnapshots/{templateSnapshotId}/groups/{groupId}/levels/{levelId}";
        String correctUrlOne = "/templateSnapshots/{templateSnapshotId}";

        assertFalse(matches(searchUrlOne, falseUrl));
        assertTrue(matches(searchUrlOne, correctUrlOne));

        String searchUrlTwo = "https://api.alpega.pw/sb-ui-dev/api/bookingListHeaders";
        String correctUrlTwo = "/bookingListHeaders";
        assertTrue(matches(searchUrlTwo, correctUrlTwo));

    }
}