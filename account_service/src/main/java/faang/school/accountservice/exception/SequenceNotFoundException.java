package faang.school.accountservice.exception;

public class SequenceNotFoundException extends RuntimeException {
    public SequenceNotFoundException(String message) {
        super(message);
    }
}
