package edu.sumdu.tss.elephant.helper.exception;

public class AccessRestrictedException extends HttpError400 {
    public AccessRestrictedException() {
        super();
    }

    public AccessRestrictedException(Exception ex) {
        super(ex);
    }

    public AccessRestrictedException(String message) {
        super(message);
    }

}
