package edu.sumdu.tss.elephant.helper.exception;

public class NotFoundException extends HttpError400 {

    public NotFoundException(String message) {
        super(message);
    }

    public Integer getCode() {
        return 404;
    }

}
