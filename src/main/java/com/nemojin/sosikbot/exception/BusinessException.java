package com.nemojin.sosikbot.exception;

public class BusinessException extends RuntimeException{
    private final BotException exception;
    private final String location;

    public BusinessException(BotException exception) {
        super(exception.getContent());
        this.exception = exception;
        this.location = extractLocation();
    }

    public String getType() {return exception.getType();}

    public String getContent() {return exception.getContent();}

    public String getOpinion() {return exception.getOpinion();}

    public String getLocation() {return location;}
    private String extractLocation() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // [0]=getStackTrace, [1]=extractLocation, [2]=생성자, [3]=예외를 던진 곳
        if (stack.length > 3) {
            StackTraceElement caller = stack[3];
            return caller.getClassName() + " :: " + caller.getLineNumber();
        }
        return "Unknown";
    }
    public String getExceptionLog() {
        return "[" + exception.getType() + "] " + exception.getContent() + "\n" + location;
    }
}
