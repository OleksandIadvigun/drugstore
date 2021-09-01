package sigma.software.leovegas.drugstore.product

class ResourceNotFoundException(message: String?) : RuntimeException(message)

class NotCorrectRequestException(message: String?) : RuntimeException(message)

class NotEnoughQuantityProductException(message: String?) : RuntimeException(message)

class InternalServerNotAvailableException(message: String?) : RuntimeException(message)