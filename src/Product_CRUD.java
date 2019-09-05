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
// import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Properties;
import API.DBAdminAPI;

/**
 * Product_CRUD class does Create/Read/Update/Delete tests on Product table.
 * 
 * @author wangguoxing
 */
public class Product_CRUD {

	// the default framework is embedded
	static String protocol = "jdbc:derby:";
	static String dbName = "OrderManagerDB";

	/**
	 * Parse single line of buffer input into array by one or more than one
	 * separated spaces
	 * 
	 * @param Input line of data from product_data.txt
	 * @return Array of String
	 */
	private static String[] parseProductData(String line) {
		String delims = "[\\t]+";
		String[] res = line.split(delims);
		return res;
	};

	/**
	 * Driver function for CRUD.
	 * 
	 * Use case: customers login in to view the available listing of all the
	 * products.
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		System.out.printf("Product_CRUD's working directory is: %s\n", System.getProperty("user.dir"));
		Properties props = new Properties();
		// providing a user name and password is optional in the embedded
		// and derbyclient frameworks
		props.put("user", "user1");
		props.put("password", "user1");
		// saved information session for this connection

		// statement is channel for sending commands thru connection
		Statement stmt = null;

		// result set for queries
		ResultSet rs = null;

		// tables tested by this program
//		String dbTables[] = { "WrittenBy", // relations
//				"Publisher", "Journal", "Article", "Author", // entities
		String dbTables[] = { "Product" };

		// name of data file
		String fileName = "/data/product_data.txt";
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
			PreparedStatement select_product = conn
					.prepareStatement("select 1 from PRODUCT where Name = ? and Description = ? and Product_SKU = ?");

			// insert operation prepared statements
			PreparedStatement insertRow_Product = conn.prepareStatement("insert into PRODUCT values(?, ?, ?)");

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
					stmt.executeUpdate("Delete from " + tbl);
					System.out.println("Truncated table " + tbl);
				} catch (SQLException ex) {
					System.out.println("Did not truncate table " + tbl);
				}
			}

			String line;
			while ((line = buffer.readLine()) != null) {

				// split input line into fields at tab delimiter
				String[] datalist = parseProductData(line);
//				System.out.println(datalist.length + " fields\n");
				if (datalist.length != 3) {
					System.out.printf("Invalid input data. Expected fields number: 3." + "Actual fields number: %d.\n",
							datalist.length);
					continue;
				}
				String sku_str = datalist[0];
				String name_str = datalist[1];
				String descrip_str = datalist[2];

				// restrict display character length to 256 for Product description
				if (datalist[2].length() > 256) {
					descrip_str = datalist[2].substring(0, 255);
				}

				select_product.setString(1, sku_str);
				select_product.setString(3, name_str);
				select_product.setString(2, descrip_str);
				// Uncertain getResult will finally fire up select query

				// use exectute() flag
				select_product.execute();
				rs = select_product.getResultSet();

				// check if product ResultSet object exists in db
				if (!rs.next()) {
					insertRow_Product.setString(1, name_str);
					insertRow_Product.setString(2, descrip_str);
					insertRow_Product.setString(3, sku_str);
					try {
						insertRow_Product.execute();
					} catch (SQLException e) {
						System.err.printf("Unable to insert Product \"%s\".\n", sku_str);
					}

					if (insertRow_Product.getUpdateCount() != 1) {
						System.err.printf("Error occured while inserting SKU: %s, Name %s, Description %s\n", sku_str,
								name_str, descrip_str);
					}
				}
				rs.close();

			}

			// print number of rows in tables
			for (String tbl : dbTables) {
				rs = stmt.executeQuery("select count(*) from " + tbl);
				if (rs.next()) {
					int count = rs.getInt(1);
					System.out.printf("After insertion, Table %s has %d rows \n", tbl, count);
				}
			}

			rs.close();

			// Test isSKU stored function
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