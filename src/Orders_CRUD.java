
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
// import java.sql.Timestamp;
// import java.text.SimpleDateFormat;
// import java.util.Date;
import java.util.Properties;
import API.DBAdminAPI;

/**
 * Create Orders table and demonstrate basic single table test.
 * 
 * @author philip gust, Andy
 */
public class Orders_CRUD {

	public static void main(String[] args) {
		// the default framework is embedded
		String protocol = "jdbc:derby:";
		String dbName = "OrderManagerDB";
		String connStr = protocol + dbName + ";create=true";

		// tables tested by this program
		String dbTables[] = { "Orders"
//			"PublishedBy", "PublishedIn", "WrittenBy",		// relations
//    	    	 	"Publisher", "Journal", "Article", "Author",		// entities
		};
		// name of data file
		String fileName = "/data/orders_data.txt";
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
				PreparedStatement insertRow_Orders = conn
						.prepareStatement("insert into Orders values(DEFAULT, ?, ?, DEFAULT, DEFAULT)");) {
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

			String line;
			while ((line = br.readLine()) != null) {
				// split input line into fields at tab delimiter
				String[] data = line.split("\t");
				if (data.length != 2) {
					System.out.printf("Invalid input data. Expected fields number: 2." + "Actual fields number: %d.\n",
							data.length);
					continue;
				}
				String customer_id = data[0];
				int amounts = Integer.valueOf(data[1]);

				// add Orders if does not exist
				try {
					// Orders_Id is set by default AUTO_INCREMENT Gensym
					insertRow_Orders.setString(1, customer_id);
					insertRow_Orders.setInt(2, amounts);
					// OrderDate is set by default Current_Timestamp
					// ShipDate is set by default null, and will be changed by "Cascading Triggers"
					insertRow_Orders.execute();
				} catch (SQLException ex) {
					// already exists
					System.err.printf("Already inserted in Orders.\n");
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

			// Print the tables
			API.PrintUtil.printOrders(conn);
			String query = "insert into Orders " + "values(DEFAULT,'cchap@foxmail.com', 1,DEFAULT,DEFAULT) ";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			conn.setAutoCommit(false);
			Savepoint saveBefore = conn.setSavepoint("saveBefore");
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

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
