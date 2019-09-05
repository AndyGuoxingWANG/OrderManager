import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Properties;

import API.DBAdminAPI;

/**
 * This test program combines all the test code in 5 seperat files. It's created after the
 * deadline of the project but does not contain new code. The test is divided into 5 parts,
 * each start with data validation for each single table and then test the impact on other
 * tables. 
 * Note: in the test I'm use PrintUtil functions to verify, but we really should use a single
 * query to make the specified tuple we are modifying on more visible.
 * @author philip gust,Andy Wang
 */
public class TestOrderManager {
	public static void main(String[] args) {
	    // the default framework is embedded
	    String protocol = "jdbc:derby:";
	    String dbName = "OrderManagerDB";
		String connStr = protocol + dbName+ ";create=true";

	    // tables tested by this program
		String dbTables[] = {
		// must be in reverse order as the last created table should be dropped first
		"OrderRecord", "Orders", "Customer", "InventoryRecord", "Product" };

		Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user1");
        props.put("password", "user1");
             
		try (
			// connect to the database using URL
			Connection conn = DriverManager.getConnection(connStr, props);

			// statement is channel for sending commands thru connection
			Statement stmt = conn.createStatement();
		) {
			System.out.println("Connected to and created database " + dbName);
			ResultSet rs = null;
			
			System.out.println("Begin testing Product Table:");
			System.out.println("Testing Data Validity");
			try {
				rs = stmt.executeQuery("values isSKU('PC-213000-1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-213000-1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-213000-1A': %s\n", ex.getMessage());
			}
			
			try {
				rs = stmt.executeQuery("values isSKU('PC-213000-1AB')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-213000-1AB') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-213000-1AB': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PC-213000-A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-213000-A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-213000-A': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PCA-213000-1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PCA-213000-1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PCA-213000-1A': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PC-21300-1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-21300-1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-21300-1A': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PC-21300-1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-21300-1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-21300-1A': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PC--21300-1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC--21300-1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC--21300-1A': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isSKU('PC-21300--1A')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isSKU('PC-21300--1A') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("SKU 'PC-21300--1A': %s\n", ex.getMessage());
			}
			
			// Print Product Table contents
			API.PrintUtil.printProducts(conn);

			// Try this query, print the result after the query is effective,
			// And then roll back this query, display the original table contents
			// Using Transactions to prevent this query affect existing data,
			// which might cause inconsistency for other tables which rely on the
			// data in this table
			String query = "delete from Product where Product_SKU = 'PC-123456-1N'";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			conn.setAutoCommit(false);
			Savepoint saveBefore = conn.setSavepoint("saveBefore");
			// inValid Product_SKU
			query = "Insert into Product values('test1','test1','P0-123456-1N')";
			System.out.printf("Executing following query: %s \n", query);
			System.out.println("Expected: Failure to insert");
			try {
				stmt.execute(query);
			} catch (SQLException e) {
				System.out.println("The check constraint 'VALIDSKU' was "
						+ "violated while performing an INSERT or UPDATE " + "on table USER1.PRODUCT.");
			}

			// Note 'PC-123456-8Z' is a valid SKU not existing in the database
			query = "Insert into Product values('MacBook Pro 15 inch 2017',"
					+ "'MacBook Pro 2017 has a new eighth-generation quad-core Intel"
					+ " processor with Turbo Boost up to 2.7 GHz. A brilliant and "
					+ "colorful Retina display with True Tone technology for a more"
					+ " true-to-life viewing experience. The latest Apple-designed "
					+ "keyboard. And the versatile Touch Bar for more ways to be "
					+ "productive. It's Apple's most powerful 11-inch notebook. "
					+ "Pushed even further.','PC-123456-8Z')";
			System.out.printf("Executing following query: %s \n", query);
			System.out.println("Expected: Failure to insert");
			try {
				stmt.execute(query);
			} catch (SQLException e) {
				System.out.println("Violate Unique Constraint.Can't add 'PC-123456-8Z'"
						+ " because the description is the same as 'PC-123456-1N'.");
			}

			conn.rollback(saveBefore);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("After Roll back, final Data in this table:");
			API.PrintUtil.printProducts(conn);		
			System.out.printf("Finished Testing Product\n\n");
			
			System.out.println("Begin testing Inventory Table:");
			// Display Inventory Table
			API.PrintUtil.printInventoryRecord(conn);

			// Try this query, display the results, and the rollback.
			query = "delete from InventoryRecord where Product_SKU = 'PC-123456-1N'";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			conn.setAutoCommit(false);
			saveBefore = conn.setSavepoint("saveBefore");

			// Test on delete cascade with SKU, delete Product_SKU will remove
			// the corresponding items in Inventory
			query = "Delete from Product where Product_SKU = 'PC-123456-00'";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printProducts(conn);
				API.PrintUtil.printInventoryRecord(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n");
			}

			// Note the UnitPrice is auto converted to two decimal digits
			query = "Insert into InventoryRecord values('PC-213000-1A',0,0.000000)";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				// API.PrintUtil.printProducts(conn);
				API.PrintUtil.printInventoryRecord(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n"); // insert successfully
			}

			query = "Delete from InventoryRecord where Product_SKU = 'PC-213000-1A' ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				// API.PrintUtil.printProducts(conn);
				API.PrintUtil.printInventoryRecord(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n"); // insert successfully
			}

			query = "Insert into InventoryRecord values('PC-213000-1A',-1,0.00) ";
			System.out.printf("Executing following query: %s \n", query);
			System.out.println("Expected: Failure to insert");
			try {
				stmt.execute(query);
				// API.PrintUtil.printProducts(conn);
				API.PrintUtil.printInventoryRecord(conn);
			} catch (SQLException e) {
				System.out.printf("Violate constraint check(AvailableUnits >= 0)\n"); // insert not successfully
			}

			query = "Insert into InventoryRecord values('PC-213000-1A', 1,-1.35) ";
			System.out.printf("Executing following query: %s \n", query);
			System.out.println("Expected: Failure to insert");
			try {
				stmt.execute(query);
				// API.PrintUtil.printProducts(conn);
				API.PrintUtil.printInventoryRecord(conn);
			} catch (SQLException e) {
				System.out.printf("Violate constraint check(UnitPrice >= 0.00)\n"); // insert not successfully
			}

			conn.rollback(saveBefore);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("After Roll back, final Data in this table:");
			API.PrintUtil.printInventoryRecord(conn);
			System.out.printf("Finished Testing Inventory\n\n");
			
			System.out.printf("Begin Testing Table Customer\n");
			// Test isCustomerIDEmailAddress stored function
			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('12345')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('12345') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address '12345': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('12345@')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('12345@') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address '12345@': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('12345@gmail')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('12345@gmail') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address '12345@gmail': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('12345@gmail.com')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('12345@gmail.com') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address '12345@gmail.com': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('wang2019@@gmail.com.com')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('wang2019@@gmail.com.com') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address 'wang2019@@gmail.com.com': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('wang2019@ntu.edu.sg')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('wang2019@ntu.edu.sg') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address 'wang2019@ntu.edu.sg': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isCustomerIDEmailAddress('wang2019@ntu.edu.sg')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isCustomerIDEmailAddress('wang2019@ntu.edu.sg') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("Email Address 'wang2019@ntu.edu.sg': %s\n", ex.getMessage());
			}

			// Test isZipCode stored function
			try {
				rs = stmt.executeQuery("values isZipCode('95123-1344')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('95123-1344') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '95123-1344': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isZipCode('95123-13444')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('95123-13444') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '95123-13444': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isZipCode('951234-1344')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('951234-1344') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '951234-1344': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isZipCode('9512-1344')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('9512-1344') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '9512-1344': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isZipCode('9512--1344')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('9512--1344') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '9512--1344': %s\n", ex.getMessage());
			}

			try {
				rs = stmt.executeQuery("values isZipCode('95123@1344')");
				rs.next();
				boolean res = rs.getBoolean(1);
				System.out.printf("value of isZipCode('95123@1344') is %b\n", res);
				rs.close();
			} catch (SQLException ex) {
				System.out.printf("PostCode '95123@1344': %s\n", ex.getMessage());
			}

			API.PrintUtil.printCustomer(conn);

			// delete one customer and then roll back
			query = "delete from Customer where Customer_ID = 'ky94ol@hotmail.com'";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			// begin test, will be rolled back to keep data clean for next table to use
			conn.setAutoCommit(false);
			saveBefore = conn.setSavepoint("saveBefore");

			query = "Insert into Customer values('Andy2','Wang2','andy@gmail.com','5800','Blossom Hill','Road', 'San Jose','TX','USA','95123')";
			System.out.printf("Executing following query: %s \n", query);

			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.printf("Aborted because caused a duplicate primary key value for andy@gmail.com \n"); // insert																												// not																												// successfully
			}

			query = "Insert into Customer values('Andy3','Wang3','andy3@gmail.com','5800','Blossom Hill','Road', 'San Jose','TU','USA','95123')";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.println("The check constraint 'STATE_CONSTRAINT' was violated"); // insert not successfully
			}

			query = "Insert into Customer values('Andy4','Wang4','andy4@gmail.com','5800','Blossom Hill','Road', 'San Jose','TX','ENG','95123')";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.println("The check constraint 'COUNTRY_CONSTRAINT' was violated"); // insert not successfully
			}

			query = "Insert into Customer values('Andy5','Wang5','andy5@gmail','5800','Blossom Hill','Road', 'San Jose','TX','ENG','95123')";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.println("The check constraint 'EMAIL_CONSTRAINT' was violated"); // insert not successfully
			}

			query = "Insert into Customer values('Andy6','Wang6','andy6@gmail.com','5800','Blossom Hill','Road', 'San Jose','TX','ENG','95123-334')";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.println("The check constraint 'ZIPCODE_CONSTRAINT' was violated"); // insert not successfully
			}

			query = "Insert into Customer values('Andy7','Wang7','andy7@gmail.com','5800',DEFAULT,DEFAULT, 'San Jose','TX','IND','95123-3525')";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
				// check for empty value for address 2 and 3
			} catch (SQLException e) {
				System.out.println("Should Not Reach Here"); // insert successfully
			}

			query = "Select Address2 from Customer where Customer_ID = 'andy7@gmail.com'";
			System.out.printf("Executing following query: %s \n", query);
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				String Address2 = rs.getString(1);
				System.out.printf("Address2 should be null by default: %s\n", Address2);
			}

			conn.rollback(saveBefore);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("After Roll back, final Data in this table:");
			API.PrintUtil.printCustomer(conn);
			System.out.printf("Finished Testing Customer.\n\n");
			
			
			System.out.println("Begin Testing Orders:");
			// Print the tables
			API.PrintUtil.printOrders(conn);
			query = "insert into Orders " + "values(DEFAULT,'cchap@foxmail.com', 1,DEFAULT,DEFAULT) ";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			conn.setAutoCommit(false);
			saveBefore = conn.setSavepoint("saveBefore");
			query = "Delete from Orders where Orders_Id = 1";
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			API.PrintUtil.printOrders(conn);

			query = "Insert into Orders " + "values(DEFAULT,'alex@gmail.com', 99,DEFAULT,DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.out.printf(
						"alex@gmail.com does not exist in Customer table," + " violates foreign key constraint\n");
			}

			// note ky94ol@hotmail.com is a valid customer email address in Customer table
			query = "Insert into Orders " + "values(DEFAULT,'ky94ol@hotmail.com', -3,DEFAULT,DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.out.printf(
						"Orders_RecordAmounts must greater than 0, -3" + " violates check(Orders_RecordAmounts > 0)\n");
			}

			// note ky94ol@hotmail.com is a valid customer email address in Customer table
			// insert successful
			query = "Insert into Orders " + "values(DEFAULT,'ky94ol@hotmail.com', 5,DEFAULT,DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n");
			}

			// test if the same customer could make other purchases since the only
			// primary key is the Orders_ID, any combination of customer email and quantity
			// of records is allowed.
			query = "Insert into Orders " + "values(DEFAULT,'ky94ol@hotmail.com', 10,DEFAULT,DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n");
			}

			query = "Insert into Orders " + "values(DEFAULT,'ky94ol@hotmail.com', 10,DEFAULT,DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.err.printf("should not reach here\n");
			}

			query = "Delete from Orders where Orders_Id = 1";
			// Note: if you run Orders_CRUD more than one time, then you should
			// change Orders_Id to 101, 201, 301....etc because the AUTO_INCREMENT
			// syntax will increase the Order_ID every time you run it.
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			API.PrintUtil.printOrders(conn);
			API.PrintUtil.printCustomer(conn);

			// test foreign key (Customer_ID) references Customer(Customer_ID) on delete
			// cascade
			query = "Delete from Customer where Customer_Id = 'andy@gmail.com'";
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			API.PrintUtil.printCustomer(conn);
			API.PrintUtil.printOrders(conn); // verify andy@gmail.com's order no longer exist

			System.out.printf("Executing following query: %s \n", query);
			query = "Delete from Customer where Customer_Id = 'ky94ol@hotmail.com'";
			stmt.execute(query);
			API.PrintUtil.printCustomer(conn);
			API.PrintUtil.printOrders(conn);
			// verify 3 tuples for ky94ol@hotmail.com's order no longer exist

			// notice that the ShipDate for all orders is null, the default value
			conn.rollback(saveBefore);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("After Roll back, final Data in this table:");
			API.PrintUtil.printOrders(conn);
			System.out.printf("Finished Testing Orders.\n\n");
				
			System.out.println("Begin Testing OrderRecord:");
			System.out.println("Check the 'AutoDeductInventory' trigger actually deduct Inventory");
			API.PrintUtil.printInventoryRecord(conn);
			query = "Select SUM(UnitAmount) from OrderRecord " + "where Product_SKU = 'PC-123456-1B' ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				rs = stmt.executeQuery(query);
				if (rs.next()) {
					int res = rs.getInt(1);
					System.out.printf("Product_SKU: PC-123456-1B has: %d units in OrderRecord\n", res);
				}
			} catch (SQLException ex) {
				System.out.println("Should not reach here");
			}
//			System.out.println("The remainding items with Product_SKU = "
//					+ "'PC-123456-1B' in Inventory should be:");
//			query = "Select ((Select AvailableUnits from InventoryRecord where (Product_SKU = 'PC-123456-1B'))-(Select SUM(UnitAmount) from OrderRecord "
//					+ "where Product_SKU = 'PC-123456-1B')) ";		
//			System.out.printf("Executing following query: %s \n",query);
//			rs = stmt.executeQuery(query);
//			if (rs.next()) {
//				int res = rs.getInt(1);
//				System.out.printf("Remaining items should be: %d\n", res);
//			}

			System.out.println("Check the 'AutoShipDate' trigger actually changed the ShipDate");
			API.PrintUtil.printOrders(conn);
			System.out.println("Check the current Boolean Status value in OrderRecord");
			API.PrintUtil.printOrderRecord(conn);

			query = "insert into OrderRecord " + "values(6,'PC-123456-0C', -9999, 3, DEFAULT)";
			System.out.printf("Executing following query: %s \n", query);
			// Note: The default status is false,the price is -9999, but the
			// "ChangeOrderRecordStatus" Trigger works
			// look at the last row, and "InsertUnitPriceInOrderRecord" trigger will reset
			// the price
			// to $ 2110.22
			// This automation will reduce human error, for example typo, which is also the
			// design concern of this project: maximize automation
			// ( 3, PC-123456-0C, $ 2110.22, 3, true)

			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			conn.setAutoCommit(false);
			saveBefore = conn.setSavepoint("saveBefore");

			query = "Insert into OrderRecord " + "values(1,'PC-123456-1C', -1, 10, DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);

			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.out.printf("'MoreRecordForOrder' Trigger: \nCan't Insert "
						+ "third record for Orders_ID:1 which only has 2 records.\n");
			}

			query = "Insert into OrderRecord " + "values(6,'PC-123456-1B', -1, -5, DEFAULT) ";
			System.out.printf("Executing following query: %s \n", query);
			try {
				stmt.execute(query);
				API.PrintUtil.printOrders(conn);
			} catch (SQLException e) {
				System.out.printf("UnitAmount should not be less than 1\n");
			}

			query = "Delete from Orders where Orders_Id = 5";
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			API.PrintUtil.printOrders(conn);
			System.out.println("Check all the Orders_Id = 5 is removed because of on delete cascade");
			API.PrintUtil.printOrderRecord(conn);

			conn.rollback(saveBefore);
			conn.commit();
			conn.setAutoCommit(true);
			System.out.println("After Roll back, final Data in this table:");
			API.PrintUtil.printOrderRecord(conn);

			query = "Delete From OrderRecord where Orders_Id = 6";
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			// Orders_ID:6 has 2 record_amounts, but only 1 record is available in
			// OrderRecord
			// Delete the last record well delete the order in Orders table because there is
			// no shipping items to fulfill- my design choice
			System.out.println("Delete last record for Orders_Id:6, remove it from Orders");
			System.out.println("First, Wake up 'NoRecordForOrder' Trigger");
			System.out.println("Check Orders_Id:6 is removed from Orders table:");
			API.PrintUtil.printOrders(conn);
			System.out.println(
					"Second, Delete last record for Orders_Id:6 also " + "Wake up 'ReturnItemsToInventory' Trigger");
			// First, this record has status = true, meaning enough stock, and the Available
			// Units in
			// InventoryRecord table is already deducted by "AutoDeductInventory" trigger
			// Second, this record has not shipped because Orders_Id:6 has 2 record_amounts.
			// Only
			// 1 of them is true will not invoke AutoShipDate trigger, rather, the ShipDate
			// is
			// is null(It's default value)

			// Then we need to return the items back to stock. It's similar to cancel
			// action.
			// Once all the Records in an Order is shipped, we do not accept cancel/return.
			// Again it's my design choice.
			System.out.printf("Check Orders_Id:6 with Product_SKU: PC-123456-00, UnitAmount:1"
					+ " is returned to Inventory Record,\n which increased the AvailableUnits"
					+ " of Product_SKU: PC-123456-00 from 69 to 70.\n");
			API.PrintUtil.printInventoryRecord(conn);
			
			// two records for Order 1 asscociated with 'mjames@husky.neu.edu' will be removed
			query = "Delete From Orders where Customer_Id = 'mjames@husky.neu.edu'";
			System.out.printf("Executing following query: %s \n", query);
			stmt.execute(query);
			API.PrintUtil.printOrderRecord(conn);			
			System.out.println("Finished Testing OrderRecord.\n\n");
		
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
