
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;
import java.math.BigDecimal;

/**
 * This program demonstrates creating and calling stored procedures and
 * functions. Since the rest of the 3 stored functions is tested in each
 * individual CRUD table, we only present a test on
 * "getUnitPriceFromInventoryRecord" stored function.
 * 
 * Note: This file has to be run after running all 5 CRUD tables
 * 
 * @author Andy
 */

public class StoredProcedure {

	/**
	 * Send mail to recipient from sender.
	 * 
	 * @param customer_id  the customer email address
	 * @param order_id    the order gensym created
	 */
	public static void sendConfirmationEmail(String customer_id, int order_id) {
		System.out.printf("Trigger: Dear %20s: Your Shopping Cart with Order_ID: %2d is confirmed."
				+ " Please add Order_Records.\n", customer_id, order_id);
		// status[0] = true;
	}

	/**
	 * Send mail to recipient from sender.
	 * 
	 * @param recipient the recipient email address
	 * @param sender    the sender email address
	 * @param status    the return status
	 */
	public static void sendEmail(String recipient, String sender, boolean status[]) {
		System.out.printf("sendEmail: To: %s  From: %s\n", recipient, sender);
		status[0] = true;
	}

	/**
	 * Determines whether 'email' is a valid email address.
	 * 
	 * @param email the email address
	 * @return true if 'email' is a valid email address
	 */
	public static boolean isEmail(String email) {
		return email.matches("^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$");
	}

	public static void InsufficientItems(String Product_SKU, int UnitAmount) {
		System.err.printf(
				"Trigger invoke stored procedure: Insertion of %d units for item: %s exceeds available amount in stock.\n",
				UnitAmount, Product_SKU);
	}

	public static void AutoShipCheck(int Orders_ID) {
		try {
			Connection conn = DriverManager.getConnection("jdbc:default:connection");

			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT COUNT(*) FROM OrderRecord where (OrderRecord.Orders_ID = ? AND OrderRecord.OrderRecord_Status = true)");
			pstmt.setInt(1, Orders_ID);
			pstmt.execute();
			ResultSet rs1 = pstmt.getResultSet();
			int True_Records = -1; // guard value
			if (rs1.next()) {
				True_Records = rs1.getInt(1);
			}
			// System.out.printf("True_Status_Records for Orders_ID %d is %d\n", Orders_ID,
			// True_Records);

			pstmt = conn.prepareStatement(
					"Update Orders SET Orders_ShipmentDate = CURRENT_TIMESTAMP where (Orders.Orders_ID = ? AND ((Select Orders_RecordAmounts from Orders where Orders_ID = ?)) = ?)");
			pstmt.setInt(1, Orders_ID);
			pstmt.setInt(2, Orders_ID);
			pstmt.setInt(3, True_Records);
			pstmt.execute();
			// System.out.printf("Record_Amount is %d\n",Record_Amount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void MoreRecordForOrder(int Orders_ID) throws SQLException {
		System.out.printf("Can't add more Order_Record for Orders_ID:%d\n ", Orders_ID);
		throw new SQLException();
	}

	public static BigDecimal getUnitPriceFromInventoryRecord(String Product_SKU) {

		BigDecimal temp = null;
		try {

			Connection conn = DriverManager.getConnection("jdbc:default:connection");
			// list unitPrice corresponding to this Product_SKU

			String query = "SELECT unitPrice FROM InventoryRecord WHERE Product_SKU = ?";
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Product_SKU);
			pstmt.execute();

			ResultSet rs = pstmt.getResultSet();
			if (rs.next()) {
				temp = rs.getBigDecimal(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return temp;
	}

	public static void main(String[] args) {
		// the default framework is embedded
		String protocol = "jdbc:derby:";
		String dbName = "OrderManagerDB";
		String connStr = protocol + dbName + ";create=true";
		String user = "user1";
		String password = "user1";

		// procedures created by this program
		String storedProcedures[] = { "sendEmail" };

		// procedures created by this program
		String storedFunctions[] = { "isEmail", "getUnitPriceFromInventoryRecord" };

		Properties props = new Properties(); // connection properties
		// providing a user name and password is optional in the embedded
		// and derbyclient frameworks
		props.put("user", user);
		props.put("password", password); // still user1

		try (
				// connect to the database using URL
				Connection conn = DriverManager.getConnection(connStr, props);
				// Connection conn = DriverManager.getConnection("jdbc:default:connection");
				// statement is channel for sending commands thru connection
				Statement stmt = conn.createStatement();) {
			System.out.println("Connected to and created database " + dbName);

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

			String createProcedure_SendEmail = "CREATE PROCEDURE sendEmail(" + "		IN recipient varchar(64),"
					+ "		IN sender varchar(64)," + "		OUT status boolean" + "	)" + " PARAMETER STYLE JAVA"
					+ " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL" + " EXTERNAL NAME"
					+ "		'StoredProcedure.sendEmail'";
			stmt.executeUpdate(createProcedure_SendEmail);
			System.out.println("Created stored procedure sendEmail");

			// call stored procedure
			CallableStatement cstmt = conn.prepareCall("call sendEmail(?,?,?)");
			cstmt.setString(1, "the_receiver@google.com"); // sender IN param
			cstmt.setString(2, "the_sender@yahoo.com"); // receiver IN param
			cstmt.registerOutParameter(3, Types.BOOLEAN); // status OUT param
			cstmt.execute();

			boolean result = cstmt.getBoolean(3); // pick up result value
			System.out.printf("sendEmail status: %b\n", result);
			cstmt.close(); // free resources when done

			// create the sendEmail stored procedure
			String createFunction_IsEmail = "CREATE FUNCTION isEmail(" + " 	email VARCHAR(64)" + "	)  RETURNS BOOLEAN"
					+ " PARAMETER STYLE JAVA" + " LANGUAGE JAVA" + " DETERMINISTIC" + " NO SQL" + " EXTERNAL NAME"
					+ "		'StoredProcedure.isEmail'";
			stmt.executeUpdate(createFunction_IsEmail);
			System.out.println("Created stored function isEmail");

			// prepared statement for calling isEmail procedure with 1 param
			PreparedStatement invoke_isEmail = conn.prepareStatement("values ( isEmail(?) )");

			// validate sender email address
			String[] senders = { "the_sender@yahoo.com", "bad.email@google" };
			for (String sender : senders) {
				invoke_isEmail.setString(1, sender);
				ResultSet rs = invoke_isEmail.executeQuery();
				if (rs.next()) {
					boolean status = rs.getBoolean(1);
					System.out.printf("sender: %s isEmail: %b\n", sender, status);
				}
				rs.close();
			}
			invoke_isEmail.close();

			// InsufficientItems

			// create the getUnitPriceFromInventoryRecord stored function
			String createFunction_getUnitPriceFromInventoryRecord = "CREATE FUNCTION getUnitPriceFromInventoryRecord("
					+ " 	Product_SKU VARCHAR(12)" + "	)  RETURNS decimal(13,2)" + " PARAMETER STYLE JAVA"
					+ " LANGUAGE JAVA" + " DETERMINISTIC" + " READS SQL DATA" + " EXTERNAL NAME"
					+ "		'StoredProcedure.getUnitPriceFromInventoryRecord'";
			stmt.executeUpdate(createFunction_getUnitPriceFromInventoryRecord);
			System.out.println("Created stored function getUnitPriceFromInventoryRecord");

			// prepared statement for calling getUnitPriceFromInventoryRecord stored
			// function
			PreparedStatement invoke_getUnitPriceFromInventoryRecord = conn
					.prepareStatement("values ( getUnitPriceFromInventoryRecord(?) )");

			// select the price from Inventory table with Valid SKU
			String[] Product_SKU_ARRAY = { "PC-123456-1B", "PC-123456-1C", "PC-123456-0C", "PC-123456-0B" };
			for (String Product_SKU : Product_SKU_ARRAY) {
				invoke_getUnitPriceFromInventoryRecord.setString(1, Product_SKU);
				ResultSet rs = invoke_getUnitPriceFromInventoryRecord.executeQuery();
				if (rs.next()) {
					BigDecimal temp = rs.getBigDecimal(1);
					double unitPrice = temp.doubleValue();
					System.out.printf("Product_SKU: %s unitPrice: %f\n", Product_SKU, unitPrice);
				}
				rs.close();
			}

			// select the price from Inventory table with InValid SKU
			System.out.println("All SKU available:");
			API.PrintUtil.printInventoryRecord(conn); // The price is available

			// Note: "PC-213000-1A" is in Product Table, but not in Inventory Table
			String[] Product_InValidSKU_ARRAY = { "PC-213000-1A", "PC-123456-1Z", "PC-123456-@@", "PCA-123456-1" };
			for (String Product_SKU : Product_InValidSKU_ARRAY) {
				invoke_getUnitPriceFromInventoryRecord.setString(1, Product_SKU);
				ResultSet rs = invoke_getUnitPriceFromInventoryRecord.executeQuery();
				if (rs.next()) {
					BigDecimal temp = rs.getBigDecimal(1); // will return null from stored function
					try {
						double unitPrice = temp.doubleValue();
						// should not reach this line
						System.out.printf("Product_SKU: %s unitPrice: %f\n", Product_SKU, unitPrice);
					} catch (NullPointerException ex) {
						System.out.printf("SKU %s: no such item in Inventory\n", Product_SKU);
					}
				}
				rs.close();
			}

			invoke_getUnitPriceFromInventoryRecord.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
