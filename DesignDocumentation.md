## DESIGN DOCUMENTATION
### GENERAL APPROACH
The system is to provide backend database support for a customer checkout pipeline where transaction record and storage management records are persisted. Once we fetch the data from the frontend and middle layer component of the basic order information and related inventory information we will store the information in the forms representing entries in a table. Then the data will be pulled by online-sheet in batch onto the databse via loders. Then through DML the data will be persisted onto the database.

### ER DIAGRAM
At the beginning, we mapped attributes into group of entities [Initial Entity Design](https://github.ccs.neu.edu/there2win/RDBMS-OrderManager/wiki/Table-Design) in terms of what is an good abstraction of properties related to that entity. Taken product and Customers for examples, the after normalized relational data model tables are built based on their attribute. 
If we encounter composite attributes(eclipse) like the ones below in Customer entity we would split the attrubute further into a spanning-tree.

Show Entity with their attributes
![](resources/CustomerEntity.png?raw=true, "Customer's attribute") 

Finally the ER model Five entities representng products, Inventory Records, customers, ordes and order record are then mapped into five tables : PRODUCT, INVENTORYRECORD,CUSTOMER,ORDERS,ORDERRECORD. where there are one-many relatinship between order and order record, customer and order, order record and product, inventory and product
![](ER_diagram.png?raw=true, 'ER diagram')

### DDL

#### STORED FUNCITONS
* "isSKU"
* "ISCUSTOMERIDEMAILADDRESS"
* "isZipCode"
* "getUnitPriceFromInventoryRecord"

Stored functions:
   * the first three perform domain constraint on the data matching table fields(SKU,EMAIL,ZIP) inserted into the database. 
   * the last one queries the price of the product through product_sku.
#### STORED PROCUDESRS 
   * "InsufficientItems" : will throw error message and block the insertion of order-record whose buying units exits stored amount in the inventory.
   * "ConfirmationEmail": will send confirm message once a new row has been inserted into the Orders.
   * "AutoShipCheck": before shippigng, check if the status flag of that Order-record see if fullfilled already.
#### TRIGGERS 
Design decisons:
* shipped order will not be returned
* unshipped order but fullfiled order record could be returned(could return while they are in shopping cart).

Description of triggers invoke case and affected outcome.

  | What  | When  |  Where(updated)|
|---|---|---|
|auto insertion of unitPrice in InventoryRecord table   | after insert on OrderRecord  |  OrderRecord |
|  check required amounts vs supply amounts | before insert on OrderRecord  |  OrderRecord |
| update status of order record  | after insert on OrderRecord   |  OrderRecord |
| deduct item count in InventoryRecord table | after status of order record set to true  | InventoryRecord  |
| add shipdate for a fullfilled order record  | after status of order record set to true  | OrderRecord  |
| simulates an email sent to customer after a new order placed  | after insert on Orders | Orders  |
| for unshipped orders a deletion in order-records will return the products in that order record back to inventory  | after delete on OrderRecord  | InventoryRecord  |
| delete a order | when no more order record associates with that order  |  Orders |
### DML
A. The following five jave files perform insert/update/delete operation of the OrderManager database.
1. Product_CRUD.java 
2. Inventory_CRUD.java 
3. Customer_CRUd.java
4. Orders_CRUD.java
5. OrderRecord_CRUD.java

B. Transactions and rolloback are supported during DML statement when performing insertion or deletion on a specific table.


### DQL
OrderManager has two a set of apis provided for roles such as database admin and business analyst. 

Based on common bussiness scenario for database. The USER_INTERFACE.java functions as a controller for directing user input (from ibserver pattern). The user input comes into two categories 
  * keywords insert (e.g option 'A' , product-sku 'AB-123456')
  * sentence insert (e.g full sql clause:  
  SELECT * FROM ORDERRECORD INNER JOIN  ORDERS O ON ORDERRECORD.ORDERS_ID = O.ORDERS_ID WHERE CUSTOMER_ID LIKE 'mj%' AND ORDERRECORD_STATUS = TRUE ORDER BY o.ORDERS_DATE

After running the application you will be directed to type in to choose which kind of user input you want at the prompt:
  * 1 for keywords
  * 2 for full sql sentence.sShould you
In option 1, it provides search functions that allow users to check up the existing database data record as if they have a dash board.
* Functions supported at this layer are: 
  * A: Show all products
  * B: search for a customer 
  * C: search for a product 
  * D: list categorical proce 
  * E: retrieve a customers' past order-Records 
In option 2, where data anlyst role could insert a string out of sql at this prompt:
> you choosed 2 now input a customized query: 

> \>\>SELECT * FROM ORDERRECORD INNER JOIN  ORDERS O ON ORDERRECORD.ORDERS_ID = O.ORDERS_ID WHERE CUSTOMER_ID LIKE 'mj%' AND ORDERRECORD_STATUS = TRUE ORDER BY o.ORDERS_DATE

>> Query result:

>1  PC-123456-1B  2120.23  4  true  1  mjames@husky.neu.edu  2  2019-06-26 20:04:49.417  2019-06-26 20:05:13.59  
>1  PC-123456-0C  2110.22  3  true  1  mjames@husky.neu.edu  2  2019-06-26 20:04:49.417  2019-06-26 20:05:13.59  
>>


### How to Run
Run the java files with imported class path and external jar file resource/derby.jar 
* step1 run **OrderManager.java** 
* step2 run **Product_CRUD.java ,Inventory_CRUD.java, Customer_CRUd.java,Orders_CRUD.java,OrderRecord_CRUD.java** sequentially.
* step3 run **StoredProcedure.java** 
* step4 run **USER_INTERFAcE.java** then follow the instruction under section DQL
