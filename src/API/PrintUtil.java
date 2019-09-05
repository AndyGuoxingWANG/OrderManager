package API;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * This class has functions for printing Product, InventoryRecord, Orders,and
 * Customers
 *
 * @author wangguoxing
 */
public class PrintUtil {

	/**
	 * Print Product table.
	 * 
	 * @param conn the connection
	 * @return number of Product rows
	 * @throws SQLException if a database operation fails
	 */
	public static int printProducts(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement();
				// list Products information ordered by Product_SKU
				ResultSet rs = stmt
						.executeQuery("select Name, Description,Product_SKU from Product order by Product_SKU");) {
			System.out.println("Products:");
			int count = 0;
			while (rs.next()) {
				String name = rs.getString(1);
				String description = rs.getString(2);
				String sku = rs.getString(3);
				System.out.printf("Name:%30s, SKU:%15s, Description: %80s\n", name, sku, description);
				count++;
			}
			System.out.printf("Tables Product has %d rows.\n", count);
			return count;
		}
	}

	/**
	 * Print InventoryRecord table.
	 * 
	 * @param conn the connection
	 * @return number of InventoryReocrd rows
	 * @throws SQLException if a database operation fails
	 */
	public static int printInventoryRecord(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement();
				// list authors and their ORCIDs
				ResultSet rs = stmt.executeQuery(
						"select Product_SKU, AvailableUnits, UnitPrice from InventoryRecord order by Product_SKU");) {
			System.out.println("InventoryRecords:");
			int count = 0;
			while (rs.next()) {
				String product_sku = rs.getString(1);
				int available_unit = rs.getInt(2);
				Double unit_price = rs.getDouble(3);

				System.out.printf("Product_SKU: %s, AvailableUnit: %3d, UnitPrice: $%8.02f\n", product_sku,
						available_unit, unit_price);
				count++;
			}
			System.out.printf("Tables InventoryRecord has %d rows.\n", count);
			return count;
		}
	}

	/**
	 * Print Customer table.
	 * 
	 * @param conn the connection
	 * @return number of Customer rows
	 * @throws SQLException if a database operation fails
	 */
	public static int printCustomer(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
		// UNCERTAIN needs confirming on best practice to print details personal data.
//				"select FamilyName, GivenName, Customer_ID, Address1, Address2, Address3, City, State, Country, PostCode from Customer order by PostCode");
				"select FamilyName, GivenName, Customer_ID, Address1 from Customer order by PostCode");) {
			System.out.println("Customers:");
			int count = 0;
			while (rs.next()) {
				String family_name = rs.getString(1);
				String given_name = rs.getString(2);
				String customer_id = rs.getString(3);
				String address_1 = rs.getString(4);
				System.out.printf("Name: %10s %10s, Email:%25s, Address:%30s\n", family_name, given_name, customer_id,
						address_1);
				count++;
			}
			System.out.printf("Tables Customer has %d rows.\n", count);
			return count;
		}
	}

	/**
	 * Print Orders table.
	 * 
	 * @param conn the connection
	 * @return number of Customer rows
	 * @throws SQLException if a database operation fails
	 */
	public static int printOrders(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(
						"select Orders_ID, Customer_ID, Orders_RecordAmounts, Orders_Date, Orders_ShipmentDate from Orders order by Orders_ID");) {
			System.out.println("Orders:");
			int count = 0;
			while (rs.next()) {
				int orders_id = rs.getInt(1);
				// String orcIdStr = Biblio.orcidToString(orcid);
				String customer_id = rs.getString(2);
				int amounts = rs.getInt(3);
				Timestamp order_date = rs.getTimestamp(4);
				Timestamp ship_date = rs.getTimestamp(5);
				System.out.printf("Orders_Id: %3d,Email: %20s, Record Amount:%2d, OrderDate: %25s, ShipDate: %25s\n",
						orders_id, customer_id, amounts, order_date, ship_date);
				count++;
			}
			System.out.printf("Tables Orders has %d rows.\n", count);
			return count;
		}
	}

	/**
	 * Print OrderRecord table.
	 * 
	 * @param conn the connection
	 * @return number of Customer rows
	 * @throws SQLException if a database operation fails
	 */
	public static int printOrderRecord(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(
						"select Orders_ID, Product_SKU, UnitPrice, UnitAmount,OrderRecord_Status from OrderRecord");) {
			System.out.println("OrderRecord:");
			int count = 0;
			while (rs.next()) {
				int orders_id = rs.getInt(1);
				String Product_SKU = rs.getString(2);
				double UnitPrice = rs.getDouble(3);
				int UnitAmount = rs.getInt(4);
				boolean OrderRecord_Status = rs.getBoolean(5);
				// Timestamp ship_date = rs.getTimestamp(5);
				System.out.printf("(%2d, %12s, $%8.2f, %2d, %5b)\n", orders_id, Product_SKU, UnitPrice, UnitAmount,
						OrderRecord_Status);
				count++;
			}
			System.out.printf("Tables OrderRecord has %d rows.\n", count);
			return count;
		}
	}

}