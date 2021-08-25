package edu.sumdu.tss.elephant.helper.exception;

public class HttpException extends RuntimeException {
    private static final String DEFAULT_ICON = "bug";

    public HttpException() {
        super();
    }

    public HttpException(Exception ex) {
        super(ex);
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Exception ex) {
        super(message, ex);
    }

    public Integer getCode() {
        return Integer.valueOf(500);
    }

    public String getIcon() {
        return DEFAULT_ICON;
    }

}
