package sim.explainer.library.exception;

import sim.explainer.library.util.ExceptionUtil;

public class JSimPiException extends RuntimeException {

    private final ErrorCode errorCode;

    public JSimPiException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public JSimPiException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Error Code: ");
        builder.append(getErrorCode());
        builder.append(" :: ");
        builder.append(ExceptionUtil.toString(this));

        return builder.toString();
    }
}
