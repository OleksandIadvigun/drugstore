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

- [x] Update Cart
- [x] Clear Cart 
- [X] Get details 
- [x] Confirm

## Product

- [x] Create
- [x] Process reception 
- [x] Process delivery 
- [x] View the most popular
- [x] Search:
    - by keyword (name...)
    - sorted by popularity (default)
    - sorted by price 
    - sorted by creation date

## Store

- [ ] Process reception
- [ ] Process delivery
- [ ] Check availability

## Accountancy

### Mandatory

- [ ] Confirm income
- [ ] Confirm outcome
- [ ] Get invoice details
- [ ] Process payment
- [ ] Refund
- [ ] Close invoice

### Optional

- [ ] Pay fixed costs
    - salary
    - rent
- [ ] Get fixed costs
- [ ] Create markups
- [ ] Get markups
- [ ] Update markups

## Analytics(to be done later)

- [ ] Calculate last month income
- [ ] Calculate last month fixed costs
- [ ] Calculate last month profit
- [ ] Calculate zero date
- [ ] Calculate potential profit date




