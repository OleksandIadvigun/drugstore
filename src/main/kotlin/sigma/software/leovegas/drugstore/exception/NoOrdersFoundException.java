package sigma.software.leovegas.drugstore.exception;

public class NoOrdersFoundException extends Exception {
    public NoOrdersFoundException() {
        super("No orders were found");
    }
}
