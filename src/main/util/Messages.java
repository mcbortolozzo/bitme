package main.util;

/**
 * Created by marcelo on 12/11/16.
 */
public enum Messages {
    DISPATCHER_RUN("dispatcher running"), CONNECTION_ACCEPT("accepting new connection"), WRITE_CONNECTION("writing to output buffer");

    private final String text;

    public String getText(){ return this.text; }

    Messages(String message) {
        this.text = message;
    }
}
