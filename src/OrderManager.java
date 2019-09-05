
import java.sql.Connection;
import java.sql.DriverManager;
// import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This program creates an OrderManager database for the ER data model for
 * Project 3. There are entity tables for Product, Inventory, Customer, and
 * relationship tables for the Order and OrderRecord relations in the ER model.
 * 
 * This version uses the relationship name for the single fields in 1:n
 * relationships between entities, rather than relationship tables. Adding
 * information to the relation will require re-factoring the database to use a
 * table for the relationship.
 * 
 * @author Andy Wang
 */
public class OrderManager {

	public static void main(String[] args) {
		// the default framework is embedded
		String protocol = "jdbc:derby:";
		String dbName = "OrderManagerDB";
		String connStr = protocol + dbName + ";create=true";

		// tables created by this program
		String dbTables[] = {
				// must be in reverse order as the last created table should be dropped first
				"OrderRecord", "Orders", "Customer", "InventoryRecord", "Product" };

		// triggers created by this program
		String dbTriggers[] = { "InsertUnitPriceInOrderRecord", "InsufficientInventory", "ChangeOrderRecordStatus",
				"AutoDeductInventory", "AutoShipDate", "SendEmailToCustomer", "ReturnItemsToInventory",
				"NoRecordForOrder", "CheckRecordAmount",
				"BackOrder"

		};

		// procedures created by this program
		String storedProcedures[] = { "InsufficientItems", "ConfirmationEmail", "AutoShipCheck", "MoreRecordForOrder"
//			"sendEmail"
		};

		// stored functions created by this program
		String storedFunctions[] = { "isSKU", "ISCUSTOMERIDEMAILADDRESS", "isZipCode", "getUnitPriceFromInventoryRecord"
//			, "isRounded"
		};

		Properties props = new Properties(); // connection properties
		// providing a user name and password is optional in the embedded
		// and derbyclient frameworks
		props.put("user", "user1");
		props.put("password", "user1");

		try (
				// connect to the database using URL
				Connection conn = DriverManager.getConnection(connStr, props);

				// statement is channel for sending commands thru connection
				Statement stmt = conn.createStatement();) {
			System.out.println("Connected to and created database " + dbName);

			// drop the database triggers and recreate them below
			for (String tgr : dbTriggers) {
				try {
					stmt.executeUpdate("drop trigger " + tgr);
					System.out.println("Dropped trigger " + tgr);
				} catch (SQLException ex) {
					System.out.println("Did not drop trigger " + tgr);
				}
			}

			// drop the database tables and recreate them below
			for (String tbl : dbTables) {
				try {
					stmt.executeUpdate("drop table " + tbl);
					System.out.println("Dropped table " + tbl);
				} catch (SQLException ex) {
					System.out.println("Did not drop table " + tbl);
				}
			}

			// drop the storedProcedures and recreate them below
			for (String proc : storedProcedures) {
				try {
					stmt.executeUpdate("drop procedure " + proc);
					System.out.println("Dropped procedure " + proc);
				} catch (SQLException ex) {
					System.out.println("Did not drop procedure " + proc);
				}
			}

			// drop the storedFunctions and recreate them below
			for (String func : storedFunctions) {
				try {
					stmt.executeUpdate("drop function " + func);
					System.out.println("Dropped function " + func);
				} catch (SQLException ex) {
					System.out.println("Did not drop function " + func);
				}
			}

			// preventing inserting new tuple to OrderRecord table
			// if the Orders_ID already contains enough Record_Amounts
			// For example, if Orders_ID:1 contains 2 records, and there
			// are already two tuples associated with Orders_ID:1, (regardless
			// of true or false), then adding a third OrderRecord for Orders_ID:1
			// is not permitted because capacity 2 is achieved.

			String createProcedure_MoreRecordForOrder = "CREATE PROCEDURE MoreRecordForOrder("
					+ "		IN orders_id int"
					// + " IN order_id int"
					// + " OUT status boolean"
					+ "	)" + " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " READS SQL DATA"
					+ " EXTERNAL NAME" + "	'StoredProcedure.MoreRecordForOrder'";
			stmt.executeUpdate(createProcedure_MoreRecordForOrder);
			System.out.println("Created stored procedure MoreRecordForOrder");

			String createProcedure_AutoShipCheck = "CREATE PROCEDURE AutoShipCheck(" + "	IN Orders_ID int"
//          		+ "		IN order_id int," 
//          		+ "		OUT status boolean"
					+ "	)" + " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " MODIFIES SQL DATA"
					+ " EXTERNAL NAME" + "	'StoredProcedure.AutoShipCheck'";
			stmt.executeUpdate(createProcedure_AutoShipCheck);
			System.out.println("Created stored procedure AutoShipCheck");

			String createProcedure_ConfirmationEmail = "CREATE PROCEDURE ConfirmationEmail("
					+ "	IN customer_id varchar(320)," + "	IN order_id int"
					// + " OUT status boolean"
					+ "	)" + " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL"
					+ " EXTERNAL NAME" + "	'StoredProcedure.sendConfirmationEmail'";
			stmt.executeUpdate(createProcedure_ConfirmationEmail);
			System.out.println("Created stored procedure ConfirmationEmail");

			// + " OUT status boolean"
			String createProcedure_InsufficientItems = "CREATE PROCEDURE InsufficientItems("
					+ "	IN Product_SKU varchar(12)," + "	IN UnitAmount int" + "	)" + " PARAMETER STYLE JAVA"
					+ " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL" + " EXTERNAL NAME"
					+ "	'StoredProcedure.InsufficientItems'";
			stmt.executeUpdate(createProcedure_InsufficientItems);
			System.out.println("Created stored procedure InsufficientItems");

			// create 7 stored functions defined in OrderManager.java
			String createFunction_isSKU = "CREATE FUNCTION isSKU(" + " Product_SKU varchar(12)" + "	)  RETURNS BOOLEAN"
					+ " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL" + " EXTERNAL NAME"
					+ "	'Validation.isSKU'";
			stmt.executeUpdate(createFunction_isSKU);
			System.out.println("Created stored function isSKU");

			String createFunction_isCustomerIDEmailAddress = "CREATE FUNCTION isCustomerIDEmailAddress("
					+ " Customer_ID varchar(320)" + "	)  RETURNS BOOLEAN" + " PARAMETER STYLE JAVA" + " LANGUAGE JAVA"
					+ " DETERMINISTIC" + " NO SQL" + " EXTERNAL NAME" + "	'Validation.isCustomerIDEmailAddress'";
			stmt.executeUpdate(createFunction_isCustomerIDEmailAddress);
			System.out.println("Created stored function isCustomerIDEmailAddress");

			String createFunction_isZipCode = "CREATE FUNCTION isZipCode(" + " PostCode varchar(11)"
					+ "	)  RETURNS BOOLEAN" + " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL"
					+ " EXTERNAL NAME" + "	'Validation.isZipCode'";
			stmt.executeUpdate(createFunction_isZipCode);
			System.out.println("Created stored function isZipCode");

			String createFunction_getUnitPriceFromInventoryRecord = "CREATE FUNCTION getUnitPriceFromInventoryRecord("
					+ " Product_SKU VARCHAR(12)" + "	)  RETURNS decimal(13,2)" + " PARAMETER STYLE JAVA"
					+ " LANGUAGE JAVA" + " DETERMINISTIC" + " READS SQL DATA" + " EXTERNAL NAME"
					+ "	'StoredProcedure.getUnitPriceFromInventoryRecord'";
			stmt.executeUpdate(createFunction_getUnitPriceFromInventoryRecord);
			System.out.println("Created stored function getUnitPriceFromInventoryRecord");

//            String createFunction_isRounded =
//          		  "CREATE FUNCTION isSKU(" 
//          		+ " 	ISSN int"
//          		+ "	)  RETURNS BOOLEAN"
//          		+ " PARAMETER STYLE JAVA"
//          		+ " LANGUAGE JAVA" 
//          		+ " DETERMINISTIC"
//          		+ " NO SQL"
//          		+ " EXTERNAL NAME"
//          		+ "		'Biblio.isIssn'";
//            stmt.executeUpdate(createFunction_isSKU);
//            System.out.println("Created stored function isSKU");

			// create the Product table
			String createTable_Product = "create table Product (" + "  Name varchar(32) not null,"
					+ "  Description varchar(255) not null unique," + "  Product_SKU varchar(12) not null,"
					+ "  primary key (Product_SKU)," + "  constraint validSKU check(isSKU(Product_SKU))" + ")";
			stmt.executeUpdate(createTable_Product);
			System.out.println("Created entity table Product");

			// create the InventoryRecord table
			// https://rietta.com/blog/best-data-types-for-currencymoney-in/
			String createTable_InventoryRecord = "create table InventoryRecord ("
					+ "  Product_SKU varchar(12) not null," + "  AvailableUnits int not null,"
					+ "  UnitPrice decimal(13,2) not null," + "  primary key (Product_SKU)," // one Product_SKU can only
																								// have one price
					// + " check(isRounded(UnitPrice))," no need as Decimal(13,2) already checked
					+ "  check(AvailableUnits >= 0)," // for future replenish if equals 0
					+ "  check(UnitPrice >= 0.00)," // free item
					+ "  foreign key (Product_SKU) references Product(Product_SKU) on delete cascade" + ")";

			stmt.executeUpdate(createTable_InventoryRecord);
			System.out.println("Created entity table InventoryRecord");

			// https://dba.stackexchange.com/questions/37014/in-what-data-type-should-i-store-an-email-address-in-database
			// 64 characters for the "local part" (username)
			// 1 character for the @ symbol
			// 255 characters for the domain name

//            Derby does not support Enum data type:	
//            On the SET datatype: Even the MySQL docs include a section upfront 
//            called "Why you shouldn't use SET" 
//            (http://dev.mysql.com/tech-resources/articles/mysql-set-datatype.html). 
//            It's not an atomic datatype. Bad. 
//
//            On ENUM: The SQL standard way of doing the equivalent of ENUM, and 
//            therefore the Derby way, is to define a check constraint on the 
//            column. 
//
//            For example, in MySQL you might do something like: 
//
//            CREATE TABLE nonstandard ( 
//              sizes ENUM('small', 'medium', 'large') 
//            ); 

			String createTable_Customer = "create table Customer(" + "  FamilyName varchar(16) not null,"
					+ "  GivenName varchar(16) not null," + "  Customer_ID varchar(320) not null," // unique email
																									// address
					+ "	 Address1 varchar(255) not null," + "	 Address2 varchar(255) default null," // could be null
					+ "	 Address3 varchar(255) default null," // could be null
					+ "  City varchar(255) not null," + "  State varchar(2) not null constraint State_Constraint"
					+ " 	check (State in ('CA','AZ','TX','KL','UP','WB','AH','GS','BJ')),"
					+ "  Country varchar(3) not null constraint Country_Constraint"
					+ "		check (Country in ('USA','IND','CHN'))," + "  PostCode varchar(10) not null,"
					// for demo purpose, only use US format: 95123-1344
					+ "  primary key (Customer_ID),"
					+ "  constraint Email_Constraint check (isCustomerIDEmailAddress(Customer_ID)),"
					+ "  constraint ZipCode_Constraint  check (isZipCode(PostCode))" + ")";
			stmt.executeUpdate(createTable_Customer);
			System.out.println("Created entity table Customer");

			// create the Order relation table
			// GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) = AUTO_INCREMENT
			String createTable_Orders = "create table Orders("
					+ "  Orders_ID int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
					+ "  Customer_ID varchar(320) not null," + "  Orders_RecordAmounts int not null,"
					+ "	 Orders_Date TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP," // check format and data type
					+ "  Orders_ShipmentDate TIMESTAMP default null," // could be null when not shipped
					+ "  primary key (Orders_ID)," // Orders_ID completely determines relation
					+ "  check(Orders_RecordAmounts > 0),"
					+ "  foreign key (Customer_ID) references Customer(Customer_ID) on delete cascade" + ")";
			stmt.executeUpdate(createTable_Orders);
			System.out.println("Created relation table Orders");

			// create the OrderRecord relation table
			// no need to check UnitPrice because eventually this price will be replaced
			// by the price in the inventory, which is checked in Inventory table

			// Here foreign key (Product_SKU) do not entail 'on delete cascade' because
			// OrderRecord is for
			// recording purpose. If a product is sold, then the record is there forever,
			// even though
			// the product is no longer in InventoryRecord(because InventoryRecord only keep
			// track of
			// current SKUs on sale), the not-on-sale item is still in the list of Product
			String createTable_OrderRecord = "create table OrderRecord(" + "  Orders_ID int not null,"
					+ "  Product_SKU varchar(12) not null," + "  UnitPrice decimal(13,2) not null,"
					+ "  UnitAmount int not null," //
					+ "  OrderRecord_Status boolean DEFAULT false," // (meaning incomplete/pending) or 1 (meaning true)
					+ "  primary key (Orders_ID,Product_SKU),"
					+ "  foreign key (Orders_ID) references Orders(Orders_ID) on delete cascade,"
					+ "  foreign key (Product_SKU) references Product(Product_SKU) on delete NO ACTION,"
					// no need to do on delete cascade
					+ "  check(UnitAmount > 0)" + ")";

			stmt.executeUpdate(createTable_OrderRecord);
			System.out.println("Created relation table OrderRecord");

			// Note: foreign key (Orders_ID) references Orders(Orders_ID) on delete cascade
			// For this foreign key, it should not only be on delete cascade, but also on
			// Update cascade: if the Orders_ID is updated, then the dependent table should
			// also update. However, there is only 'on update no action' syntax in derby
			// mySQL could use on update cascade

			// create trigger to inform a customer when there is a new tuple in Orders table
			// but has nothing to do with orderRecord
			String createTrigger_SendEmailToCustomer = "create trigger SendEmailToCustomer" + " after insert on Orders"
					+ " REFERENCING NEW ROW AS NewTuple" + " for each row MODE DB2SQL"
					+ "	CALL ConfirmationEmail(NewTuple.CUSTOMER_ID, NewTuple.Orders_ID)";

			stmt.executeUpdate(createTrigger_SendEmailToCustomer);
			System.out.println("Created trigger for SendEmailToCustomer");

			// create trigger for checking if there is no more record for this Orders_ID
			// in OrderRecord Table
			String createTrigger_CheckRecordAmount = "create trigger CheckRecordAmount" + " after insert on OrderRecord"
					+ " REFERENCING NEW ROW AS NewTuple" + " for each row MODE DB2SQL"
					+ " WHEN ((SELECT Orders_RecordAmounts from Orders where Orders_ID = NewTuple.Orders_ID) < "
					+ "	  	(SELECT count(*) from OrderRecord "
					+ "	  	where (OrderRecord.Orders_ID = NewTuple.Orders_ID)))"
					+ "	CALL MoreRecordForOrder(NewTuple.Orders_ID)";

			stmt.executeUpdate(createTrigger_CheckRecordAmount);
			System.out.println("Created trigger for CheckRecordAmount");

			// create trigger for auto insertion of unitPrice in InventoryRecord table
			String createTrigger_InsertUnitPriceInOrderRecord = "create trigger InsertUnitPriceInOrderRecord"
					+ " after insert on OrderRecord" + " REFERENCING NEW ROW AS NewTuple" + " for each row MODE DB2SQL"
					+ " UPDATE OrderRecord" + "	  SET UnitPrice = " + "	  	(SELECT UnitPrice from InventoryRecord "
					+ "	  	where InventoryRecord.Product_SKU = NewTuple.Product_SKU)"
					+ "	  where Product_SKU = newTuple.Product_SKU";

			stmt.executeUpdate(createTrigger_InsertUnitPriceInOrderRecord);
			System.out.println("Created trigger for InsertUnitPriceInOrderRecord");

			// create trigger for preventing inserting orderRecord with UnitAmount
			// greater than available units in InventoryRecord
			String createTrigger_InsufficientInventory = "create trigger InsufficientInventory"
					+ " NO CASCADE BEFORE insert on OrderRecord" + " REFERENCING NEW ROW AS NewTuple"
					+ " for each row MODE DB2SQL" + " WHEN (NewTuple.UnitAmount >"
					+ "	  	(SELECT AvailableUnits from InventoryRecord "
					+ "	  	where InventoryRecord.Product_SKU = NewTuple.Product_SKU))"
					+ "	CALL InsufficientItems(NewTuple.Product_SKU, NewTuple.UnitAmount)";

			stmt.executeUpdate(createTrigger_InsufficientInventory);
			System.out.println("Created trigger for InsufficientInventory");

			// set status to false only when two attributes of the primary key satisfied
			String createTrigger_ChangeOrderRecordStatus = "create trigger ChangeOrderRecordStatus"
					+ " AFTER insert on OrderRecord" + " REFERENCING NEW ROW AS NewTuple" + " for each row MODE DB2SQL"
					+ " WHEN (NewTuple.UnitAmount <=" + "	  	(SELECT AvailableUnits from InventoryRecord "
					+ "	  	where InventoryRecord.Product_SKU = NewTuple.Product_SKU))"
					+ "	UPDATE OrderRecord set OrderRecord_Status = true"
					+ " 	where (OrderRecord.Product_SKU = NewTuple.Product_SKU"
					+ " and OrderRecord.Orders_ID = NewTuple.Orders_ID)";

			stmt.executeUpdate(createTrigger_ChangeOrderRecordStatus);
			System.out.println("Created trigger for ChangeOrderRecordStatus");

			String createTrigger_AutoDeductInventory = "create trigger AutoDeductInventory"
					+ " AFTER Update OF OrderRecord_Status" + " on OrderRecord" + " REFERENCING NEW ROW AS NewTuple"
					+ " for each row MODE DB2SQL" + " Update InventoryRecord"
					+ "	  	set AvailableUnits = AvailableUnits - NewTuple.UnitAmount"
					+ " 		where (InventoryRecord.Product_SKU = NewTuple.Product_SKU)";
			stmt.executeUpdate(createTrigger_AutoDeductInventory);
			System.out.println("Created trigger for AutoDeductInventory");

			String createTrigger_AutoShipDate = "create trigger AutoShipDate" + " AFTER Update OF OrderRecord_Status"
					+ " on OrderRecord" + " REFERENCING NEW ROW AS NewTuple" + " for each row MODE DB2SQL"
					+ "	CALL AutoShipCheck(NewTuple.Orders_ID)";
			stmt.executeUpdate(createTrigger_AutoShipDate);
			System.out.println("Created trigger for AutoShipDate");

			// This trigger is only for demonstration purpose to show that in order to
			// achieve one automation, we could make a 'pure' trigger, or write a java
			// program using prepared statement, wrap it to a procedure, and then
			// make a trigger to call the stored procedure
//          String createTrigger_AutoShipDate2 =
//          		  "create trigger AutoShipDate2"
//          		+ " AFTER Update OF OrderRecord_Status"
//          		+ " on OrderRecord"
//          		+ " REFERENCING NEW ROW AS NewTuple"
//          		+ " for each row MODE DB2SQL"
//          		+ " WHEN((SELECT COUNT(*) FROM OrderRecord"
//          		+ " 	where (NewTuple.Order_ID = OrderRecord.Order_ID"
//          		+ "			AND OrderRecord.OrderRecord_Status = true)) ="
//          		+ " (SELECT Orders_RecordAmounts from Orders where (Orders.Order_ID = NewTuple.Order_ID))"
//          		+ " )"
//                + "	SET Orders.Orders_ShipmentDate = CURRENT_TIMESTAMP"
//                + " 	where Orders.Orders_ID = NewTuple.Orders_ID";
//          		      		 
//          stmt.executeUpdate(createTrigger_AutoShipDate2);
//          System.out.println("Created trigger for AutoShipDate2");

			// if status = false, amount not deducted from Inventory
			// if order shipped together, can't accept return.
			// only return items to Inventory when status = true and not shipped yet.
			String createTrigger_ReturnItemsToInventory = "create trigger ReturnItemsToInventory" + " AFTER DELETE "
					+ " on OrderRecord" + " REFERENCING OLD ROW AS OldTuple" + " for each row MODE DB2SQL"
					+ " WHEN ( (OldTuple.OrderRecord_Status = true) AND "
					+ " ((Select Orders_ShipmentDate From Orders where Orders_ID = OldTuple.Orders_ID) is null))"
					+ " Update InventoryRecord" + "	  	set AvailableUnits = AvailableUnits + OldTuple.UnitAmount"
					+ " 		where (InventoryRecord.Product_SKU = OldTuple.Product_SKU)";
			stmt.executeUpdate(createTrigger_ReturnItemsToInventory);
			System.out.println("Created trigger for ReturnItemsToInventory");

			// If there is no more Records(regardless of In-Stock/True or False/Back-Order
			// status),
			// Delete the Order_ID because there is no more records associated with it.
			String createTrigger_NoRecordForOrder = "create trigger NoRecordForOrder" + " AFTER DELETE "
					+ " on OrderRecord" + " REFERENCING OLD ROW AS OldTuple" + " for each row MODE DB2SQL"
					+ " WHEN ((Select COUNT(*) FROM OrderRecord Where OrderRecord.Orders_ID = OldTuple.Orders_ID) = 0)"
					+ " Delete From Orders" + " 	where (Orders.Orders_ID = OldTuple.Orders_ID)";
			stmt.executeUpdate(createTrigger_NoRecordForOrder);
			System.out.println("Created trigger for NoRecordForOrder");

			// If there is no available amount, the status is false
			// BackOrder trigger checks after Inventory is replenished (AvailableUnits must increase),
			// and auto-fulfills the OrderRecord with a false status
//          String createTrigger_BackOrder =
//          		  "create trigger BackOrder"
//          		+ " AFTER UPDATE OF AvailableUnits "
//          		+ " on InventoryRecord"
//          		+ " REFERENCING Old ROW AS OldTuple"
//          		+ " for each row MODE DB2SQL"
//          	    + " WHEN ((Select AvailableUnits FROM InventoryRecord Where InventoryRecord.Product_SKU  = OldTuple.Product_SKU)>OldTuple.AvailableUnits)"
//          		+ " UPDATE OrderRecord SET "	
//        		+ " OrderRecord.OrderRecord_Status = true "
//        		+ "	WHERE ( (OrderRecord.OrderRecord_Status = false) AND "
//        		+ "	UnitAmount in (Select UnitAmount From OrderRecord where (Select UnitAmount FROM OrderRecord Where  "
//        		+ " (OrderRecord.Product_SKU = OldTuple.Product_SKU)) < "
//        		+ " (Select AvailableUnits FROM InventoryRecord Where InventoryRecord.Product_SKU  = OldTuple.Product_SKU)"
//        		+ "))";
//          stmt.executeUpdate(createTrigger_BackOrder);
//          System.out.println("Created trigger for BackOrder");

//			  If using mySQL, then we implement Assertion for inserting invalid value.
//            String createAssertion_OrderRecord_UnitAmount =
//            			"create assertion ASSE_OrderRecord_UnitAmount("
//            		+ "	 CHECK(UnitAmount <= ALL (SELECT AvailableUnits FROM InventoryRecord "
//            		+ "	 GROUP by Product_SKU,UnitPrice)"
//            		+ ") ";	
//            stmt.executeUpdate(createAssertion_OrderRecord_UnitAmount);
//            System.out.println("Created assertion ASSE_OrderRecord_UnitAmount");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
