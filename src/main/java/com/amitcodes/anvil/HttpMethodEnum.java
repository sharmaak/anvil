package com.amitcodes.anvil;

public enum HttpMethodEnum {

    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE");

    String method;

    private HttpMethodEnum(String method) {
        this.method = method;
    }

    public String toString() {
        return method;
    }

    public static HttpMethodEnum fromString(String methodName) {
        if(methodName == null || methodName.trim().length()==0) {
            throw new IllegalArgumentException("Method name is null or empty");
        }

        methodName = methodName.trim().toUpperCase();
        if(methodName.equals("GET"))          { return GET;    }
        else if (methodName.equals("PUT"))    { return PUT;    }
        else if (methodName.equals("POST"))   { return POST;   }
        else if (methodName.equals("DELETE")) { return DELETE; }

        throw new IllegalArgumentException(String.format("Invalid http method: '%s", methodName));
    }
}
