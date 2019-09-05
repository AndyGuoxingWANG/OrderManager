import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Properties;
import java.util.Scanner;

import API.DBAdminAPI;
import API.PrintUtil;

/**
 *  this is a demo of showing how users could interact with DB with provided APIS
 * @author Theodore
 *
 */

public class USER_INTERFACE {
	
	// the default framework is embedded
    static String protocol = "jdbc:derby:";
    static String dbName = "OrderManagerDB";
	static String connStr = protocol + dbName+ ";create=true";


	public static void establishConnection(String usr_name, String password_str) {
		Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", usr_name);
        props.put("password", password_str);
        // connect to the database using URL
 		String connStr = protocol + dbName + ";create=true";
 		Connection conn = null;
 		ResultSet rs = null;
        try {
        	
        	// connect to database
			conn = DriverManager.getConnection(connStr, props);
			Statement stmt = conn.createStatement();
//        	if(conn == null) System.out.println("ERROORRR!!!");
        	System.out.println(conn.toString());
			System.out.println("Connected to and created database \n");
			// call controller
			messageBroker(conn);
			System.out.println("Ending connetion ...");
			conn.close();	
        }
        catch (NullPointerException e) {
        	e.printStackTrace();
        }
        catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
	
	public static void messageBroker(Connection conn) throws SQLException {
		System.out.println("Press any key then hit 'ENTER' to continue... \n");
		System.out.print(">> ");
		// accepting input from console
		Scanner scan = new Scanner(System.in);
		boolean isOver = false;
		while(scan.hasNext()) {
			System.out.print(">> ");
			String input_str = scan.nextLine();
			
			System.out.println(" Use OrderManager: \n" + "1 for Provided options\n" + "2 for Customized query\n");
			switch(input_str.toUpperCase()) {
				case "EXIT":
					isOver = true;
					break;
				case "1":
					System.out.print("you choosed 1, which options do what want ? \n");
					System.out.println("A: Show all products \n" 
					+ "B: search for a customer \n"
					+ "C: search for a product \n"
					+ "D: list categorical proce \n"
					+ "E: retrieve a customers' past order-Records \n"
					);
					System.out.print(">> ");
					String input = scan.nextLine();
					if(input.equals("A")) {
						System.out.println("You choosed to view all products ! \n");
						PrintUtil.printProducts(conn);
						break;
					} else if(input.equals("B")) {
						System.out.println("You choosed to search for a customer, Input the customer_email pattern:");
						System.out.print("\n>> ");
						// input support abbreviation such as mj%
						String customer_str = scan.nextLine();
						DBAdminAPI.searchCustomer(conn, customer_str);
						break;
					} else if(input.equals("C")) {
						System.out.println("You choosed to search for a product in inventory, Input the product_sku:");
						System.out.print("\n>> ");
						// input support abbreviation such as mj%
						String product_sku = scan.nextLine();
						DBAdminAPI.searchProduct(conn, product_sku);
						break;
					} else if(input.equals("D")) {
						System.out.println("You choosed to show products in a category with price listed high to low:");
						System.out.println("Input the product category regex \n"+ "e.g PC%");
						System.out.print("\n>>");
						String category_str = scan.nextLine();
						String category_upperstr = category_str.toUpperCase();
						DBAdminAPI.lowerToHigherPrice(conn, category_upperstr);
						break;
					}
						else if(input.equals("E")){
						System.out.println("You choosed to show a customers' fullfilled orders:");
						System.out.println("Input the customer email  \n"+ "e.g abc@hotmail.com ");
						System.out.print("\n>> ");
						String customer_id = scan.nextLine();
						DBAdminAPI.customerPastOrderRecords(conn, customer_id);
						break;
					} else break;
				case "2":
					System.out.print("you choosed 2 \n + now input a customized query: \n");
					System.out.print(">> ");
					String query = scan.nextLine();
					DBAdminAPI.customisedQuery(conn, query);
					break;
				default:
					System.out.println("Input either 1 for Premise options 2 for Customized query");
					System.out.print("\n>> ");
					break;
			}
			if(isOver) break; 
		}	
	}
	
	
	public static void main(String[] args) {
		establishConnection("user1", "user1");
	}
	
	
}
