package com.papple.framework.util;

import org.apache.commons.codec.binary.Base64;


public class Utility {

    public static String base64Decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        byte[] decoded = Base64.decodeBase64(encoded);
        return new String(decoded);
    }

    public static String base64Encode(String s) {
        if (s == null) {
            return s;
        }
        byte[] encoded = Base64.encodeBase64(s.getBytes());
        return new String(encoded);
    }
}
