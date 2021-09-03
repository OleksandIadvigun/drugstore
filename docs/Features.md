# Features

```

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
```                                 

## Table of Content

| №| Feature |
| ----------- | ----------- |
| 1.  | [Order](#order)
| 2.  | [Product](#product)
| 3.  | [Store](#store)
| 4.  | [Accounting](#accounting)
| 5.  | [Analytics](#analytics)

## Order

Mandatory
- [x] Create order
- [x] Update order
- [x] Get order by id
- [x] Get orders
- [X] Get orders by status
- [X] Get order details by order id
- [X] Change order status
- [x] Get total buys of the order items
- [x] Checkout order

## Product

Mandatory
- [x] Create product
- [x] Reduce Quantity
- [x] Receive products
- [x] Get products details by ids
- [x] Get available products:
    - sorted by popularity (default)
- [x] Search products:
    - by keyword (name...)
    - sorted by popularity (default)
    - sorted by price 
    - sorted by creation date

## Store

Mandatory
- [ ] Create new transfer acceptance item
- [ ] Get transfer acceptance items
- [ ] Get transfer acceptance items by invoice ids
- [ ] Accept products 
- [ ] Deliver products 
- [ ] Check availability of products

## Accountancy

Mandatory
- [ ] Create Income Invoice
- [ ] Create Outcome Invoice
- [ ] Get Invoice by id
- [ ] Cancel Invoice
- [ ] Refund Invoice
- [ ] Pay Invoice

Optional
- [ ] Create fixed costs
- [ ] Update fixed costs
- [ ] Get fixed costs
- [ ] Get markups
- [ ] Update markups

## Analytics(to be done later)

- [ ] Calculate last month income
- [ ] Calculate last month purchase costs
- [ ] Calculate last month costs
- [ ] Calculate last month profit
- [ ] Calculate zero date
- [ ] Calculate potential profit date




