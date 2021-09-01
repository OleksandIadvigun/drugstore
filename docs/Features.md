# Features


                                 P DB (products)                              __O
                                                                                |\
                                  ^                                            / \
                                  |                                             |
                                         GET order items available for sail     V
        +-------(W2)-------->  PRODUCT  <-------------(R)-------------------  ORDER --> O DB (orders)
        |                                                                       
        |                         ^                                             ^
        |                         | POST invoice                                |
        NP -> STORE                      (W) what                                      (W) POST
        ^                         |  how many                                   |   pid: qty
        |                         |  price per product                          |
        |    GET by invoice id                                                  |
        +--------(R1)-------> ACCOUNTANT <--------------------------------------+

                                  |
                                  V
                                 A DB (invoices) 

## Table of Content

| №| Feature |
| ----------- | ----------- |
| 1.  | [Order](#order)
| 2.  | [Product](#product)
| 3.  | [Store](#store)
| 4.  | [Accounting](#accounting)
| 5.  | [Analytics](#analytics)

### Order

- [x] Create order
- [x] Update order
- [x] Get order by id
- [x] Get orders
- [X] Get orders by status
- [X] Get order details by order id
- [X] Change order status
- [x] Get total buys of the order items

### Product

- [x] Create product
- [x] Update product
- [x] Get product by id
- [x] Get products by ids
- [x] Get products:
    - sorted by total buys (default)
    - sorted by name (ASC/DESC)
- [x] Search products:
    - by keyword (name...)
    - sorted by popularity (default)

### Store

- [X] Create new store item
- [X] Get store items
- [X] Get store items by price item ids
- [X] Increase store item's quantity
- [X] Reduce store item's quantity
- [X] Check availability of products
- [X] Delivery of goods

### Accountancy

- [X] Create new price item
- [X] Update new price item
- [X] Get products' price
- [X] Get products' price by ids
- [X] Create Invoice
- [X] Get Invoice by id
- [X] Cancel Invoice
- [X] Get Refund
- [X] Pay Invoice
- [X] Create purchased costs
- [X] Update purchased costs
- [X] Get purchased costs
- [ ] Create costs
- [ ] Update costs
- [X] Get markups
- [X] Update markups
- [ ] Get past purchased items
- [X] Get past purchased items
- [ ] Create costs
- [ ] Update costs
- [ ] Credit, debit ??? account details

### Analytics

- [ ] Calculate last month income
- [ ] Calculate last month purchase costs
- [ ] Calculate last month costs
- [ ] Calculate last month profit
- [ ] Calculate zero date
- [ ] Calculate potential profit date




