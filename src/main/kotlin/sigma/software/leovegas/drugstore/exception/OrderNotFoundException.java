package sigma.software.leovegas.drugstore.exception;

public class OrderNotFoundException extends Exception {
    public OrderNotFoundException(Long id) {
        super("No order with id= " + id + " was found");
    }
}
