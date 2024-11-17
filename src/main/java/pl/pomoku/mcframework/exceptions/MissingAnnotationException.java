package pl.pomoku.mcframework.exceptions;

public class MissingAnnotationException extends RuntimeException {
    public MissingAnnotationException(Class<?> clazz) {
        super("Missing annotation " + clazz.getSimpleName());
    }
}
