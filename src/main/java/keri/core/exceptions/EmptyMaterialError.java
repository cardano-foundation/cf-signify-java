package keri.core.exceptions;

public class EmptyMaterialError extends RuntimeException {
    private final String message;

    public EmptyMaterialError(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
