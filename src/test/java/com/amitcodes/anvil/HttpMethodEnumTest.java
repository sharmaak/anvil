package com.amitcodes.anvil;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class HttpMethodEnumTest {

    public static final String GET="GET";
    public static final String PUT="PUT";
    public static final String POST="POST";
    public static final String DELETE="DELETE";


    @Test
    public void testFromStringGet() {
        HttpMethodEnum method = HttpMethodEnum.fromString("gEt");
        assertEquals(GET, method.method);
    }

    @Test
    public void testFromStringPut() {
        HttpMethodEnum method = HttpMethodEnum.fromString("pUt");
        assertEquals(PUT, method.method);
    }

    @Test
    public void testFromStringPost() {
        HttpMethodEnum method = HttpMethodEnum.fromString("PoSt");
        assertEquals(POST, method.method);
    }

    @Test
    public void testFromStringDelete() {
        HttpMethodEnum method = HttpMethodEnum.fromString("dElEte");
        assertEquals(DELETE, method.method);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testFromStringNull() {
        HttpMethodEnum.fromString(null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testFromStringInvalidString() {
        HttpMethodEnum.fromString("LoremIpsm");
    }

    @Test
    public void testToStringGet() {
        assertEquals(GET, HttpMethodEnum.GET.toString());
    }

    @Test
    public void testToStringPut() {
        assertEquals(PUT, HttpMethodEnum.PUT.toString());
    }

    @Test
    public void testToStringPost() {
        assertEquals(POST, HttpMethodEnum.POST.toString());
    }

    @Test
    public void testToStringDelete() {
        assertEquals(DELETE, HttpMethodEnum.DELETE.toString());
    }
}
