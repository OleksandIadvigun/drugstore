package sigma.software.leovegas.drugstore.accountancy

open class AccountancyServiceException(message: String) : RuntimeException(message)

open class OrderAlreadyConfirmedException(orderNumber: Long) :
    AccountancyServiceException("Order($orderNumber) already has invoice.")

class InvoiceAlreadyPaidException(orderNumber: Long?) :
    AccountancyServiceException("Order($orderNumber) already paid. Please, first do refund.")

class ProductIdCannotBeNullException
    : AccountancyServiceException("Product id cannot be null or negative.")

class OrderContainsInvalidProductsException(productNumbers: List<Number> = listOf()) :
    AccountancyServiceException("Order contains invalid products: ${productNumbers.joinToString(separator = ", ")}.")

class InvalidStatusOfInvoice() :
    AccountancyServiceException("The invoice status should be CREATED to be paid, but status found is invalid.")

class InvoiceNotFoundException(orderNumbers: Long) :
    AccountancyServiceException("Invoice of Order($orderNumbers) not found.")

class NotPaidInvoiceException(id: Long) : AccountancyServiceException("The invoice with id = $id is not paid.")

class ProductServiceResponseException(message: String) :
    AccountancyServiceException("Ups... some problems in product service. $message.]")

class OrderServiceResponseException(message: String) :
    AccountancyServiceException("Ups... some problems in accountancy service. $message.]")

class StoreServiceResponseException(message: String) :
    AccountancyServiceException("Ups... some problems in accountancy service. $message.]")

class NotEnoughMoneyException() : AccountancyServiceException("Not enough money for this transaction.")

class ProductsItemsAreEmptyException() : AccountancyServiceException("Products items should be not empty.")
