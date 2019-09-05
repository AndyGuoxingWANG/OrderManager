
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
// import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
// import java.sql.Timestamp;
// import java.sql.Types;
import java.util.Properties;
import API.DBAdminAPI;

/**
 * Create OrderRecord table and demonstrate basic single table test. NOTE: Can't
 * run this java file twice.
 * 
 * If you want to duplicate the result, please run OrderManager and then all the
 * 5 tables.
 * 
 * @author philip gust, Andy
 */
public class OrderRecord_CRUD {

	public static void main(String[] args) {
		// the default framework is embedded
		String protocol = "jdbc:derby:";
		String dbName = "OrderManagerDB";
		String connStr = protocol + dbName + ";create=true";

		// tables tested by this program
		String dbTables[] = { "OrderRecord"
//			"PublishedBy", "PublishedIn", "WrittenBy",		// relations
//    	    	 	"Publisher", "Journal", "Article", "Author",		// entities
		};

		// name of data file
		String fileName = "/data/order_record_data.txt";
		String filePath = new File("").getAbsolutePath();
		String readerInput = (filePath + fileName);

		Properties props = new Properties(); // connection properties
		// providing a user name and password is optional in the embedded
		// and derbyclient frameworks
		props.put("user", "user1");
		props.put("password", "user1");

		// result set for queries
		ResultSet rs = null;
		try (
				// open data file
				BufferedReader br = new BufferedReader(new FileReader(new File(readerInput)));

				// connect to database
				Connection conn = DriverManager.getConnection(connStr, props);
				Statement stmt = conn.createStatement();

				// insert prepared statements
				PreparedStatement insertRow_OrderRecord = conn
						.prepareStatement("insert into OrderRecord values(?, ?, ?, ?, DEFAULT)");) {
			// connect to the database using URL
			System.out.println("Connected to database " + dbName);

			// clear data from tables
			for (String tbl : dbTables) {
				try {
					stmt.executeUpdate("delete from " + tbl);
					System.out.println("Truncated table " + tbl);
				} catch (SQLException ex) {
					System.out.println("Did not truncate table " + tbl);
				}
			}
			System.out.println("Before adding OrderRecord, the AvailableUnits in Inventory:");
			API.PrintUtil.printInventoryRecord(conn);

			String line;
			while ((line = br.readLine()) != null) {
				// split input line into fields at tab delimiter
				String[] data = line.split("\t");
				if (data.length != 3) {
					System.out.printf("Invalid input data. Expected fields number: 3." + "Actual fields number: %d.\n",
							data.length);
					continue;
				}
				int orders_id = Integer.valueOf(data[0]);
				String product_sku = data[1];
				int unit_amount = Integer.valueOf(data[2]); // the number of unit for this SKU
				// Timestamp order_date = Timestamp.valueOf(data[2]);

				// add OrderRecord if does not exist
				try {
					// Both setString and setInt for orders_id works, weird right?
					// insertRow_Orders.setString(1, orders_id);
					insertRow_OrderRecord.setInt(1, orders_id);
					insertRow_OrderRecord.setString(2, product_sku);

					// Since the unitPrice is not null defined in the ProducRecord Table,
					// Have to set the Price to the guard value here, and then change the Price
					// either by: 1. Trigger "InsertUnitPriceInOrderRecord"
					// 2. Stored Function "getUnitPriceFromInventoryRecord"
					// I implemented both, the commented-out code below is using stored function
					// To retrieve the UnitPrice and then using that price for the prepared
					// statement
					double unitPrice = -1; // safeguard value, will be changed

//		        	// prepared statement for calling isEmail procedure with 1 param
//		        	PreparedStatement invoke_getUnitPriceFromInventoryRecord =
//		        	conn.prepareStatement("values ( getUnitPriceFromInventoryRecord(?) )");
//		        	
//		        	invoke_getUnitPriceFromInventoryRecord.setString(1, product_sku);
//		        	rs = invoke_getUnitPriceFromInventoryRecord.executeQuery();
//		        	if (rs.next()) {
//		        		unitPrice = rs.getBigDecimal(1).doubleValue();
//		        		System.out.printf("Product_SKU: %s unitPrice: %f\n", product_sku, unitPrice);
//		        	}
//		            // rs.close();
//		        	
//		        	invoke_getUnitPriceFromInventoryRecord.close();

					// Here the UnitPrice is still -1 and trigger will change the UnitPrice
					insertRow_OrderRecord.setDouble(3, unitPrice);
					insertRow_OrderRecord.setInt(4, unit_amount); // amount of this SKU product

					// the 5th ? is the default false status: not enough stock
					try {
						insertRow_OrderRecord.execute();
					} catch (SQLException ex) {
						// System.err.println(ex.getMessage());
						System.err.printf("Already inserted OrderRecord with Orders_ID %d and SKU %s\n", orders_id,
								product_sku);
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					System.err.printf("Insert failed because trigger haven't set price >= 0\n");
				}

			}
			// print number of rows in tables
			for (String tbl : dbTables) {
				rs = stmt.executeQuery("select count(*) from " + tbl);
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.printf("Table %s : count: %d\n", tbl, count);
				}
			}
			rs.close();

			System.out.println("Check the 'AutoDeductInventory' trigger actually deduct Inventory");
			API.PrintUtil.printInventoryRecord(conn);
			String query = "Select SUM(UnitAmount) from OrderRecord " + "where Product_SKU = 'PC-123456-1B' ";
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
			Savepoint saveBefore = conn.setSavepoint("saveBefore");

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
			
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
