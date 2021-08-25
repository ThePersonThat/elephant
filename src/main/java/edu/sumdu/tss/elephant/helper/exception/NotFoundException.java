package edu.sumdu.tss.elephant.helper.exception;

public class NotFoundException extends HttpError400 {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Class klass, String condition) {
        super("Not found " + klass + " by condition " + condition);
    }

    public NotFoundException(Class klass, int id) {
        super("Not found " + klass + " by ID " + id);
    }

    public Integer getCode() {
        return Integer.valueOf(404);
    }

}
