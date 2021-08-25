package edu.sumdu.tss.elephant.helper.exception;

public class BackupException extends HttpError500 {
    public BackupException() {
        super();
    }

    public BackupException(Exception ex) {
        super(ex);
    }

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Exception ex) {
        super(message, ex);
    }

}
