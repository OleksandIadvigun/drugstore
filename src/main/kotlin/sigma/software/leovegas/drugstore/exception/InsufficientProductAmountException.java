package sigma.software.leovegas.drugstore.exception;

public class InsufficientProductAmountException extends Exception{
    public InsufficientProductAmountException() {
        super("You have to add minimum one product to create the order");
    }
}
