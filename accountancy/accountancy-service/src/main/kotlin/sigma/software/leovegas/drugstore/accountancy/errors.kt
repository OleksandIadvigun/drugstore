package sigma.software.leovegas.drugstore.accountancy

open class AccountancyServiceException(message: String) : RuntimeException(message)

open class OrderAlreadyConfirmedException(orderNumber: Long) :
    AccountancyServiceException("Order($orderNumber) already has invoice")

class InvoiceAlreadyPaidException(orderNumber: Long?) :
    AccountancyServiceException("Order($orderNumber) already paid. Please, first do refund")

class OrderContainsInvalidProductsException(productNumbers: List<Number> = listOf()) :
    AccountancyServiceException("Order contains invalid products: ${productNumbers.joinToString(separator = ", ")}")

class InvalidStatusOfInvoice() :
    AccountancyServiceException("The invoice status should be CREATED to be paid, but status found is invalid")

class InvoiceNotFoundException(orderNumbers: Long) :
    AccountancyServiceException("Invoice of Order($orderNumbers) not found.")

class NotPaidInvoiceException(id: Long) : AccountancyServiceException("The invoice with id = $id is not paid")

class ProductServiceResponseException() :
    AccountancyServiceException("Ups... Something went wrong! Please, try again later")

class OrderServiceResponseException() :
    AccountancyServiceException("Ups... Something went wrong! Please, try again later")

class StoreServiceResponseException() :
    AccountancyServiceException("Ups... Something went wrong! Please, try again later")

class NotEnoughMoneyException() : AccountancyServiceException("Not enough money for this transaction")
