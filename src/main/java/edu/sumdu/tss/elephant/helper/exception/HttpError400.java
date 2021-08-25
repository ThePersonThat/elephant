package edu.sumdu.tss.elephant.helper.exception;

public class HttpError400 extends HttpException {

    public HttpError400() {
        super();
    }

    public HttpError400(Exception ex) {
        super(ex);
    }

    public HttpError400(String message) {
        super(message);
    }

    public HttpError400(String message, Exception ex) {
        super(message, ex);
    }

    public Integer getCode() {
        return Integer.valueOf(500);
    }

}
