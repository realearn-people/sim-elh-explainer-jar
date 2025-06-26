package sim.explainer.library.util;


import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {

    public static String toString(Throwable t) {
        StringBuilder builder = new StringBuilder();

        builder.append(t.getClass().getName());
        builder.append(":");
        builder.append(t.getLocalizedMessage());

        StackTraceElement[] stackTraceElements = t.getStackTrace();
        if(stackTraceElements != null) {
            for(StackTraceElement element : stackTraceElements) {
                builder.append("\n\tat ");
                builder.append(element);
            }
        }

        builder.append("\n");

        Throwable cause = ExceptionUtils.getCause(t);

        if (cause != null) {
            builder.append(ExceptionUtils.getStackTrace(cause));
        }

        return builder.toString();
    }
}
