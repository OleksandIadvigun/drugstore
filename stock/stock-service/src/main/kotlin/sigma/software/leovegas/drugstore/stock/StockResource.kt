//package sigma.software.leovegas.drugstore.stock
//
//import org.springframework.http.HttpStatus
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("api/v1/stocks")
//class StockResource(private val service: StockService) {
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    fun create(@RequestBody stock: StockRequest): StockResponse = service.create(stock)
//
//    @GetMapping
//    @ResponseStatus(HttpStatus.OK)
//    fun getStocks(): List<StockResponse> = service.getAll()
//
//    @GetMapping("/{id}")
//    @ResponseStatus(HttpStatus.OK)
//    fun getOne(@PathVariable id: Long): StockResponse = service.getOne(id)
//
//    @PutMapping("/{id}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    fun update(@PathVariable id: Long, @RequestBody stock: StockRequest): StockResponse = service.update(id, stock)
//
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    fun delete(@PathVariable id: Long) = service.delete(id)
//}
