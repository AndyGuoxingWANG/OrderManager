package API;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * API for DQL interface
 * @author Theodore
 *
 */
public class DBAdminAPI {
	
	/**
	 * Function performs a customized query on a single table print the result then roll-back to the state before the execution
	 * @param conn connection generated by derby driver
	 * @param stmt statement generated by connection
	 * @param query input SQL query
	 * @param table_name target table query aims at.
	 */
	public static void transactionRollback(Connection conn, Statement stmt, String query, String table_name) {
	    	try {
				conn.setAutoCommit(false);
				Savepoint saveBefore = conn.setSavepoint("saveBefore");
			// delete certain product.
			System.out.printf("\n executing following query: %s \n",query);
			stmt.execute(query);
			int after_delete_count = 0;
			int final_count = 0;
			switch(table_name.toUpperCase()) {
				case "PRODUCT":
					after_delete_count = PrintUtil.printProducts(conn);
					System.out.printf("After execution, Table %s has %d rows \n", table_name, after_delete_count);
					conn.rollback(saveBefore);
					final_count = PrintUtil.printProducts(conn);
					break;
				case "INVENTORYRECORD":
					after_delete_count = PrintUtil.printInventoryRecord(conn);
					System.out.printf("After execution, Table %s has %d rows \n", table_name, after_delete_count);
					conn.rollback(saveBefore);
					final_count = PrintUtil.printInventoryRecord(conn);
			  		break;
				case "CUSTOMER":
					after_delete_count = PrintUtil.printCustomer(conn);
					System.out.printf("After execution, Table %s has %d rows \n", table_name, after_delete_count);
					conn.rollback(saveBefore);
					final_count = PrintUtil.printCustomer(conn);
					break;
				case "ORDERS":
					after_delete_count = PrintUtil.printOrders(conn);
					System.out.printf("After execution, Table %s has %d rows \n", table_name, after_delete_count);
					conn.rollback(saveBefore);
					final_count = PrintUtil.printOrders(conn);
					break;
				case "ORDERRECORD":
					after_delete_count = PrintUtil.printOrderRecord(conn);
					System.out.printf("After execution, Table %s has %d rows \n", table_name, after_delete_count);
					conn.rollback(saveBefore);
					final_count = PrintUtil.printOrderRecord(conn);
					break;
				default:
					System.out.print(table_name);
					throw new IllegalArgumentException("The table name is invalid");
			}
			System.out.printf("After Roll Back, Table %s has %d rows \n", table_name, final_count);
			// commit after roll-back
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * For any given customer_id input return boolean result after querying id.
	 * @param con connection generated by derby driver
	 * @param customer_id input customer string first few characters
	 * @return boolean expression after querying the input string pattern
	 */
	public static boolean containsCustomerID(Connection con, String customer_id) {
		String query = String.format("SELECT  COUNT(*) as count FROM CUSTOMER WHERE CUSTOMER_ID LIKE '%s'",customer_id);
		System.out.println(query);
		try ( 
			Statement stmt = con.createStatement();
			ResultSet rs  = stmt.executeQuery(query);
			  ){
			  while(rs.next()){
				  if(rs.getInt("count") == 1) return true;
			  }
			  return false;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	/**
	 *  If one or multiple customer_id matches the input pattern print out a listing of matched customers.
	 * @param con connection generated by derby driver
	 * @param customer_str input customer_id pattern e.g an%  will list result for  andy@email... or as well as andrea@email...
	 */
	public static void searchCustomer(Connection con, String customer_str) {
		if(containsCustomerID(con, customer_str)) {
			System.out.println("Customer exists , prints brief information as below:");
			String query = String.format("SELECT GIVENNAME, FAMILYNAME, CUSTOMER_ID, POSTCODE FROM CUSTOMER WHERE CUSTOMER_ID LIKE '%s' ORDER BY GIVENNAME ",customer_str);
			try (Statement stmt = con.createStatement();
					// list customers information ordered by Product_SKU
					ResultSet rs = stmt
							.executeQuery(query);) {
				System.out.println("Matched results:");
				while (rs.next()) {
					String fname = rs.getString(1);
					String lname = rs.getString(2);
					String customer_id = rs.getString(3);
					String postcode = rs.getString(4);
					System.out.printf("Customer:  %s_%s (%s) %s \n",fname, lname, customer_id,postcode);
				}
				return;
			}catch (SQLException e) {
				e.printStackTrace();
				}
		}
		System.out.printf("Searched customer does not exist in DB");
	} 
	
	/**
	 * For any given order_id input return boolean result after querying id.
	 * @param con connection generated by derby driver
	 * @param order_id input order_id in form of string representing integer 
	 * e.g '101'
	 * @return boolean expression for the query result
	 */
	public static boolean containsOrderID(Connection con, String order_id) {
		String query = "SELECT COUNT(*) as count FROM ORDERS WHERE ORDERS_ID = ".concat(order_id);
		System.out.println(query);
		try ( 
			Statement stmt = con.createStatement();
			ResultSet rs  = stmt.executeQuery(query);
			  ){
			  while(rs.next()){
				  if(rs.getInt("count") == 1)
					  return true;
			  }
			  return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	
	
	/**
	 *  For any given product_sku input return boolean result after querying id.
	 * @param con connection generated by derby driver
	 * @param product_sku  input string representation of product_sku
	 * @return 
	 */
	public static boolean containsProduct(Connection con, String product_sku) {
		String query = String.format("SELECT COUNT(*) as count FROM PRODUCT WHERE Product_SKU = '%s'",product_sku);
		System.out.println(query);
		try ( 
			Statement stmt = con.createStatement();
			ResultSet rs  = stmt.executeQuery(query);
			  ){
			  while(rs.next()){
				  if(rs.getInt("count") == 1) 
					  return true;
			  }
			  return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	/**
	 *  If product exist print out the listing of that product
	 * @param con database connection generated by derby driver
	 * @param product_sku input string representation of product_sku
	 */
	public static void searchProduct(Connection con, String product_sku) {
		if(containsProduct(con, product_sku)) {
			System.out.println("Searched product exist, prints as below:");
			String query = String.format("SELECT  Name, Description,Product_SKU FROM PRODUCT WHERE Product_SKU = '%s'",product_sku);
			try (Statement stmt = con.createStatement();
					// list Products information ordered by Product_SKU
					ResultSet rs = stmt
							.executeQuery(query);) {
				System.out.println("Products:");
				while (rs.next()) {
					String name = rs.getString(1);
					String description = rs.getString(2);
					String sku = rs.getString(3);
					System.out.printf("Product %s, %s (%s)\n",sku, name, description);
				}
				return;
			}catch (SQLException e) {
				e.printStackTrace();
				}
		}
		System.out.printf("Searched products does not exist in DB");
	} 
	
	/**
	 * Print most expensive product for a given category in inventory
	 * @param con database connection generated by derby driver
	 * @param category_code_str string representation of first two digit of product_sku
	 */
	public static void  lowerToHigherPrice(Connection con, String category_code_str) {
		// pass the  category_code_str (followed by %)in the form of first two digit of SKU.
		// reference:	https://db.apache.org/derby/docs/10.0/manuals/tuning/perf96.html
		String query =String.format("SELECT PRODUCT_SKU, AvailableUnits, UNITPRICE FROM INVENTORYRECORD WHERE PRODUCT_SKU LIKE '%s' ORDER BY UNITPRICE DESC",category_code_str);
		System.out.println(query);
		try ( 
			Statement stmt = con.createStatement();
			ResultSet rs  = stmt.executeQuery(query);
			  ){
			System.out.printf("Price of %s categorty listed from high to low :", category_code_str.replace('%', ' ') );
			  while(rs.next()){
				  // follow the format -sku-unit-price
				  String product_sku = rs.getString(1);
				  Double price = rs.getDouble(3);
				  Integer units = rs.getInt(2);
				  System.out.printf("\n Price : $%8.02f , Units: %d  , Product_SKU: %s \n", price, units, product_sku);
			  }
			 
			  return;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	
	/**
	 * Show all the OrderRecords related to one customer
	 * @param con  database connection generated by derby driver
	 * @param customer_id customer_str input customer_id pattern e.g an%  will list result for  andy@email... or as well as andrea@email...
	 */
	public static void customerPastOrderRecords(Connection con, String customer_id) {
		// first ensure customer exist
		if(!containsCustomerID(con,customer_id)) {
			System.out.printf("\n The input User: %s does not exist", customer_id);
		} else {
			String query =String.format("SELECT * FROM ORDERRECORD "
					+ "INNER JOIN  ORDERS O "
					+ "ON ORDERRECORD.ORDERS_ID = O.ORDERS_ID "
					+ "WHERE CUSTOMER_ID LIKE '%s' AND ORDERRECORD_STATUS = TRUE "
					+ "ORDER BY o.ORDERS_DATE", customer_id);//accept input such as 'mj%'
			try(	
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(query);
					){
				System.out.printf("The customer: %s 's past orders orders ranked by orderDate: ",customer_id );
				while (rs.next()) {
					  // the result type is dependent on the table schema defined in OrderManager.java 
					  String order_id = rs.getString(1);
					  // passed customer_id = rs.getString(2);
					  String order_date = rs.getTimestamp("ORDERS_DATE").toString();
					  String product_sku = rs.getString(2);
					  Double product_price = rs.getDouble(3);
					  Integer order_unit = rs.getInt(4);
					  boolean fullfilled_status = rs.getBoolean(4);
					  System.out.printf("\n Order_id : %s ,Ordered_date: %s. Product_SKU: %s, Product_amont: %d, Product_price: %f , fullfilled status: %b \n",
							  order_id,order_date, product_sku, order_unit, product_price, fullfilled_status);
				}
			}catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
	}
	
	/**
	 * If product exist print out the listing of that product.
	 * @param con database connection generated by derby driver
	 * @param input_query string of SQL query
	 * e.g SELECT COUNT(CUSTOMER_ID), STATE FROM CUSTOMERS GROUP BY STATE
	 */
		public static void customisedQuery(Connection con, String input_query) {

			try (Statement stmt = con.createStatement();
					// list Products information ordered by Product_SKU
					ResultSet rs = stmt
							.executeQuery(input_query);
					
				
					)
			{
				
				System.out.println("Query result:");
				while (rs.next()) {
					ResultSetMetaData rsmt = rs.getMetaData();
					int column_cnt = rsmt.getColumnCount();
					System.out.println('\n');
					for (int i = 0;i <column_cnt; i++ ) {
						String cur_line = rs.getString(i + 1); 
						System.out.printf(" %s ", cur_line);
					}
					System.out.print(" \n");
				}
				return;
			}catch (SQLException e) {
				System.err.printf("Invalid SQL query, insert again: ");
				}
			
			System.out.printf("Invalid SQL query, insert again: ");
		} 
	

	
}
