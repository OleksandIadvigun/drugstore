```gherkin
| №  | Feature    |
| 1. | Order      |
| 2. | Product    |
| 3. | Store      |
| 4. | Accounting |
| 5. | Analytics  |

Feature: Order

  Scenario: As a user I should create order
    Given previously created order items (product ids, quantity)
    When send Post Request with order items
    Then receive response with response body:
    """
    id
    orderStatus = CREATED
    list order items 
    createdAt
    updatedAt    
    """

  Scenario: As a user I should update order
    Given previously created order
    When send Put Request with with new content
    Then receive response with response body equal to an order with:
    """
    status = UPDATED,
    updatedAt greater than equals createdAt
    """

  Scenario: As a user I should get order by id
    Given previously created order
    When send Get Request with id
    Then receive response with response body equal to created order

  Scenario: As a user I should get orders
    Given previously created orders
    When send Get Request
    Then receive response with response body equal to created orders

  Scenario: As a user I should get orders by status
    Given previously created orders with different statuses
    When send Get Request with status
    Then receive response with response body equal to order with 
    """
    status = status
    """

  Scenario: As a user I should get order details by order id
    Given previously created order
    When send Get Request with id
    Then receive response with response body:
    """
    order items names,
    quantities,
    their price,
    and total amount
    """

  Scenario: As a user I should change order status
    Given previously created order
    When send Put Request with new status
    Then receive response with response body equal to order with:
    """
    status = new status
    updatedAt greater than equals updatedAt of created order 
    """

  Scenario: As a user I should get total buys of the order items
    Given previously created order
    When send Get Request
    Then receive response with response body as a map:
    """
    product id - quantity
    """


Feature: Product

  Scenario: As a user I should create product
    Given name
    When send Post Request with name
    Then receive response with response body: 
    """
    id,
    name
    """

  Scenario: As a user I should update product
    Given previously created product
    When send Put Request with new content
    Then receive response with response body equal to product with
    """
    name = new content
    """

  Scenario: As a user I should get product by id
    Given previously created products
    When send Get Request with id
    Then receive response with response body equal to created order

  Scenario: As a user I should get products by ids
    Given previously created products
    When send Get Request with ids
    Then receive response with response body equal to created products with:
    """ 
    product item ids equal to ids
    """

  Scenario: As a user I should get products
    Given previously created products
    When send Get Request
    Then receive response with response body equal to created in descending or ascending products order by:
    """ 
    sorted by total buys (popularity), or
    sorted by name, or
    sorted by price
    """

  Scenario: As a user I should search products
    Given previously created products
    When send Get Request with searchName
    Then receive response with response body in descending or ascending order by:
    """
    products' id,
    names = searchName,
    their prices
    
    sorted by total buys (popularity), or
    sorted by name, or
    sorted by price
    """


Feature: Store

  Scenario: As a user I should create store item (first time added product with quantity)
    Given price item id, quantity
    When send Post Request with given content
    Then receive response with response body:
    """
    id,
    price item id,
    quantity
    """

  Scenario: As a user I should get store items
    Given preciously created store items
    When send Get Request
    Then receive response with response body equal to store items

  Scenario: As a user I should get store items by price item ids
    Given preciously created store items
    When send Get Request with ids
    Then receive response with response body equal to store items with:
    """
    store item ids equal to ids
    """

  Scenario: As a I should increase quantity
    Given preciously created store item
    When send Put Request with quantity to increase
    Then receive response with response body equal to store item with:
    """
    quantity greater than quantity of created store item
    """

  Scenario: As as user I should reduce quantity of store item
    Given preciously created store item
    When send Put Request with quantity to reduce
    Then receive response with response body equal to store item with:
    """
    quantity less than quantity of created store item
    """

  Scenario: As a user I should check availability of store items
    Given preciously created store items
    When send Put Request with ids and quantities
    Then receive response with message
    """
    Available
    """

  Scenario: As a user I should deliver the goods
    Given previously created order
    When send Post Request with id
    Then receive response with message:
    """
    Delivered
    """


Feature: Accounting

  Scenario: As a user I should create price item (first time added product with price)
    Given product id, price
    When send Post Request with given content
    Then receive response with response body:
    """
    id,
    product id,
    price,
    createdAt,
    updatedAt
    """

  Scenario: As a user I should update price item
    Given previously created price item
    When send Put Request with new price
    Then receive response with response body equal to price item with:
    """
    price = new price
    updatedAt greater than createdAt
    """

  Scenario: As a user I should get price items
    Given previously created price items
    When send Get Request
    Then receive response with response body equal to price items

  Scenario: As a user I should get price items by ids
    When send Get Request with ids
    Then receive response with response body equal to price items with:
    """
     id,
    product id,
    price,
    createdAt,
    updatedAt
    """

  Scenario: As a user I should create invoice
    Given previously created order
    When send Post request with order id
    Then receive response with response body:
    """
    id,
    order id,
    invoice status = CREATED,
    createdAt,
    expireAt,
    order items name,
    order items price,
    order items quantity,
    and total amount
    """

  Scenario: As a user I should get invoice by id
    Given previously created invoice
    When send Post request with id
    Then receive response with response body equal to created invoice

  Scenario: As a user I should cancel invoice
    Given previously created invoice
    When send Put request
    Then receive response with response body equal to created invoice with status:
    """
    status = CANCELLED
    """

  Scenario: As a user I should get refund
    Given previously created invoice
    When send Put request
    Then receive response with response body equal to created invoice with status:
    """
    invoice status = REFUND
    """

  Scenario: As a user I should pay invoice
    Given previously created invoice
    When send Put request
    Then receive response with response body equal to created invoice with status:
    """
    invoice status = PAID
    """

  Scenario: As a user I should get past purchased goods
    Given previously created paid invoices
    When send Get request
    Then receive response with response body:
    """
    product ids,
    their prices,
    quantities
    """

  Scenario: As a user I should calculate last month income
    Given previously created paid invoices
    When send Get request
    Then receive response with response body:
    """
    income
    """

  Scenario: As a user I should calculate last month purchase costs
    Given previously created price items
    When send Get request
    Then receive response with response body:
    """
    costs
    """


Feature: Analytics

  Scenario: As a user I should add costs
    Given rent, salary, products-costs
    When send Post request with given content
    Then receive response with response body:
    """
    id,
    rent,
    salary,
    product-costs,
    createdAt,
    updatedAt
    """

  Scenario: As a user I should update costs
    Given previously created costs
    When send Post request with new content
    Then receive response with response body equals to created costs with:
    """
    rent = new rent
    salary = new salary
    product-costs = new product-cost
    updatedAt greater than equals createdAt
    """

  Scenario: As a user I should calculate last month costs
    Given previously created costs
    When send Get request
    Then receive response with response body:
    """
    costs
    """

  Scenario: As a user I should calculate last month profit
    Given previously created costs, income
    When send Get request
    Then receive response with response body:
    """
    profit
    """

  Scenario: As a user I should calculate zero date
    Given previously created costs, income
    When send Get request
    Then receive response with response body:
    """
    date
    """

  Scenario: As a user I should calculate potential profit date
    Given previously created costs, income
    When send Get request
    Then receive response with response body:
    """
    date
    """
```
