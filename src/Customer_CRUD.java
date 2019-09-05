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
import java.util.Properties;

import API.DBAdminAPI;
//import API.PrintUtil;

/**
 * This programs deal with populating Customer table with information retrieved
 * from online form source such as google sheet. Customer_CRUD class does
 * Create/Read/Update/Delete tests on Customer table.
 * @author wangguoxing
 */
public class Customer_CRUD {

	static String protocol = "jdbc:derby:";
	static String dbName = "OrderManagerDB";

	/**
	 * Parse single line of buffer input into array by one or more than one
	 * separated tabs
	 * 
	 * @param line
	 * @return Array of String
	 */
	private static String[] parseCustomerData(String line) {
		String delims = "[\\t]";
		String[] res = line.split(delims, -1); // only apply pattern once
		return res;
	};

	/**
	 * Driver function for Customer_CRUD. Use case: administrator login in to view
	 * the available listing of all the customers.
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		System.out.printf("Program customer_crud's working Directory: %s\n", System.getProperty("user.dir"));
		Properties props = new Properties();
		// providing a user name and password is optional in the derby embedded client
		// framework.
		props.put("user", "user1");
		props.put("password", "user1");

		// statement is channel for sending commands thru connection
		Statement stmt = null;

		// result set for queries
		ResultSet rs = null;

		// tables tested by this program
		String dbTables[] = { "Customer" };

		// name of data file
		String fileName = "/data/customer_data.txt";
		// concatenate relative file path
		String filePath = new File("").getAbsolutePath();
		String readerInput = (filePath + fileName);// .replaceAll("\\s+","");
		// connect to the database using URL
		String connStr = protocol + dbName + ";create=true";
		try (
				// open data file
				BufferedReader buffer = new BufferedReader(new FileReader(new File(readerInput)));
				// connect to database
				Connection conn = DriverManager.getConnection(connStr, props);) {

			stmt = conn.createStatement();
			System.out.println("Connected to and created database " + dbName);

			// select operation prepared statements
			PreparedStatement select_product = conn.prepareStatement("select * from CUSTOMER where FamilyName = ? "
					+ "and GivenName = ? " + "and Customer_ID = ? " + "and Address1 = ?" + "and Address2 = ?"
					+ "and Address3 = ?" + "and City = ? " + "and State = ? " + "and Country = ?" + "and PostCode = ?");

			// insert operation prepared statements
			PreparedStatement insertRow_Customer = conn
					.prepareStatement("insert into CUSTOMER values(?,?,?,?,?,?,?,?,?,?)");

			// show data before insertion
			for (String tbl : dbTables) {
				rs = stmt.executeQuery("select count(*) from " + tbl);
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.printf("Before insertion, Table %s has %d rows \n", tbl, count);
				}
			}

			// clear data from tables
			for (String tbl : dbTables) {
				try {
					stmt.executeUpdate("delete from " + tbl);
					System.out.println("Truncated table " + tbl);
				} catch (SQLException ex) {
					System.out.println("Did not truncate table " + tbl);
				}
			}
			int row_cnt = 1;
			String line;
			while ((line = buffer.readLine()) != null) {
				String[] datalist = parseCustomerData(line);

				if (datalist.length < 7) {
					System.err.printf("Row: %d has invalid input data. Expected fields at least: 7."
							+ "Actual fields number: %d.\n", row_cnt, datalist.length);

					continue;
				}
				row_cnt++;
//				String family_name = datalist[0];
//				String given_name = datalist[1];
				String customer_id = datalist[2];
//				String address_1 = datalist[3];
//				String address_2 = datalist[4];
//				String address_3 = datalist[5];
//				String city = datalist[6];
//				String state = datalist[7];
//				String country = datalist[8];
				String postcode = datalist[9];

				// Should not use java functions to do logic check, the stored functions will do
				// the check.
				if (!Validation.isCustomerIDEmailAddress(customer_id) || !Validation.isZipCode(postcode))
					continue;

				// assert datalist.length == 10;
				for (int i = 0; i < datalist.length; i++) {
					select_product.setString(i + 1, datalist[i]);
				}

				// use execute() flag
				select_product.execute();
				rs = select_product.getResultSet();
				// check if product ResultSet object exists in db
				if (!rs.next()) {
					for (int i = 0; i < datalist.length; i++) {
						insertRow_Customer.setString(i + 1, datalist[i]);
					}
					// catch and show the line where the input data caused SQL to crash due to
					// truncating error.
					try {
						insertRow_Customer.execute();
					} catch (SQLException e) {
						System.err.printf("Invalid tuple at row:%d in customer_data.txt\n", row_cnt);
					}
					if (insertRow_Customer.getUpdateCount() != 1) {
						System.err.printf("Error occured while inserting row %s.\n", row_cnt);
					}
				}
				rs.close();
			}

			// print number of rows in tables
			for (String tbl : dbTables) {
				rs = stmt.executeQuery("select count(*) from " + tbl);
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.printf("After insertion, Table %s has %d rows.\n", tbl, count);
				}
			}
			rs.close();

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
			String query = "delete from Customer where Customer_ID = 'ky94ol@hotmail.com'";
			DBAdminAPI.transactionRollback(conn, stmt, query, dbTables[0]);

			// begin test, will be rolled back to keep data clean for next table to use
			conn.setAutoCommit(false);
			Savepoint saveBefore = conn.setSavepoint("saveBefore");

			query = "Insert into Customer values('Andy2','Wang2','andy@gmail.com','5800','Blossom Hill','Road', 'San Jose','TX','USA','95123')";
			System.out.printf("Executing following query: %s \n", query);

			try {
				stmt.execute(query);
				API.PrintUtil.printCustomer(conn);
			} catch (SQLException e) {
				System.out.printf("Aborted because caused a duplicate primary key value for andy@gmail.com \n"); // insert
																													// not
																													// successfully
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

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				// close statement
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
