package edu.sumdu.tss.elephant.helper.exception;

public class HttpError500 extends HttpException {

    public HttpError500() {
        super();
    }

    public HttpError500(Exception ex) {
        super(ex);
    }

    public HttpError500(String message) {
        super(message);
    }

    public HttpError500(String message, Exception ex) {
        super(message, ex);
    }

}
