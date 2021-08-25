package edu.sumdu.tss.elephant.helper.exception;

public class NotImplementedException extends HttpError500 {

    public Integer getCode() {
        return Integer.valueOf(500);
    }

    public String getIcon() {
        return "construct";
    }

    public String getMessage() {
        return super.getMessage() == null ? "Under construction" : super.getMessage();
    }

}
