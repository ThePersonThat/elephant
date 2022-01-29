package edu.sumdu.tss.elephant.helper;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HmacTest {

    @Test
    @SneakyThrows
    @DisplayName("Should generate valid hmac")
    void testCalculateWithValidValues() {
        String path = "/api/v1/database/database1/create";
        String privateKey = "zavzgztghqmywkxxaizx";
        String expectedMac = "45a9b4cef16839d4d8588a9d16821d6156b013c1a760a887cc551e91213d0f9d7d67a67e1869aa3fe7bf9ef983cf6497";

        String actualHMac = Hmac.calculate(path, privateKey);

        assertEquals(expectedMac, actualHMac);
    }
}