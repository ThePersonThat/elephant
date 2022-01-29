package edu.sumdu.tss.elephant.helper;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PairTest {


    @DisplayName("Should create Pair with default null fields")
    @Test
    void keyAndValueShouldBeNull() {
        Pair defaultPair = new Pair<>();

        assertNull(defaultPair.getKey());
        assertNull(defaultPair.getValue());
    }

    @DisplayName("Should create Pair with particular fields")
    @Test
    void keyAndValueShouldHaveValues() {
        String str1 = "str1";
        String str2 = "str2";

        Pair<String, String> pair = new Pair<>(str1, str2);

        assertEquals(str1, pair.getKey());
        assertEquals(str2, pair.getValue());
    }

}
