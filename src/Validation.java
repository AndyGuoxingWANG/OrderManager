import java.math.BigDecimal;

/**
 * This file supports all the stored functions which is used as validation
 * purposes in check constraint.
 * 
 * @author: Theodore, Andy
 */
public class Validation {

	public static final BigDecimal ZERO = BigDecimal.ZERO;

	/**
	 * Parse single line of buffer input into array by one or more than one
	 * separated spaces.
	 * 
	 * @param line the input string
	 * @return string[] res string after parsing
	 */
	public static String[] parseProductxt(String line) {
		String delims = "[ ]+";
		String[] res = line.split(delims);
		return res;
	};

	/**
	 * Determine whether parsed data is valid sku definition of sku: ( the SKU is a
	 * 12-character value of the form AA-NNNNNN-CC where A is an upper-case letter,
	 * N is a digit from 0-9, and C is either a digit or an uppper case letter. For
	 * example, "AB-123456-0N") reference: Bibilo.java(P.Gust)
	 * 
	 * @param pku_str String pku_str is the input product sku string
	 * @return boolean result to indicate if the String is SKU or not
	 */
	public static boolean isSKU(String pku_str) {
		// alpha-numerical pattern
//    	String pattern= "^[a-zA-Z0-9]*$";
		String sku_pattern = "(^[A-Z]{2})-(\\d{6})-([A-Z0-9]{2})$";
		return pku_str.matches(sku_pattern) && pku_str.length() == 12;
	};

	/**
	 * Determine whether parsed data string is valid price(rounded) definition of
	 * rounded price:
	 * @param priceStr the input price string
	 * @return boolean result to indicate if the the price string contains 2 digits or not
	 */
	public static boolean isRounded(String priceStr) {
		String round_pattern = "(\\d*).(\\d{2})$";

		return priceStr.matches(round_pattern);
	}
	
	/**
	 * Helper Function transform string into Decimal
	 * @param price_str
	 * @return a double value of a valid price string
	 */
	public static Double strToDecimal(String price_str) {

		try {
			if (Double.parseDouble(price_str) >= 0) {
				return Double.parseDouble(price_str);
			}
			return -1.00;

		} catch (Exception e) {
			return -1.00;
		}
	}

	/**
	 * Function determine whether string represents a proper product count instead
	 * throwing numerical exceptions, unqualified string will return false
	 */
	public static boolean isProperCount(String str) {
//    	if(str.matches("^-?\\d+$"))
		try {
			if (Integer.parseInt(str) > 0)
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Determine whether customer-id is valid definition: use valid email address
	 * for customerID.
	 * @param a string representing customer email address
	 * @return a boolean result indicate if the input string is an email address or not
	 */
	public static boolean isCustomerIDEmailAddress(String id_str) {
//    	if(!id_str.matches("^[0-9]*$")) {
//    		return false;
//    	} 
//    	
//    	BigDecimal id_num = stinroDecimal(id_str);
//    	
//    	return id_str.matches("\\d*") && (id_num.compareTo(ZERO) == 1);
		// use email address as valid id.
		// pattern src: https://emailregex.com/
		String pattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?"
				+ "^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-"
				+ "\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")"
				+ "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]"
				+ "*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
				+ "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\"
				+ "x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-"
				+ "\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		return id_str.matches(pattern);

	}

	/**
	 * Determine whether zip code is validCode A valid US ZIP code recognizes both
	 * the five-digit and nine-digit (ZIP + 4) e.g. a valid postal code should match
	 * 12345 and 12345-6789, but not 1234, 123456, 123456789, or 1234-56789.
	 * 
	 * @param zip_str an input string representing zipCode
	 * @return if the input string is a valid US zipCode or not
	 */
	public static boolean isZipCode(String zip_str) {
		String pattern = "^[0-9]{5}(?:-[0-9]{4})?$"; // for demo purpose, only work for US zip code
		return zip_str.matches(pattern);
	}

	// test all the above validation java functions that is wrapped for stored functions
	public static void main(String[] args) {

		// test isSku
		String sku_str = "AB-123456-0N";
		String not_sku_str1 = "AB-123B-0N";
		String not_sku_str4 = "A1-123B-0N";
		String not_sku_str2 = "AB-1234B-0N";
		String not_sku_str3 = "11-123424-bB ";
		String not_sku_str5 = "ACB-123424-bB ";
		String not_sku_str6 = "ACB-123424-bB1 ";

		System.out.println("testing isSKU function \n");
		assert isSKU(sku_str) == true : "valid sku";
		assert isSKU(not_sku_str1) == false : "invalid sku1";
		assert isSKU(not_sku_str2) == false : "invalid sku2";
		assert isSKU(not_sku_str3) == false : "invalid sku3";
		assert isSKU(not_sku_str4) == false : "invalid sku4";
		assert isSKU(not_sku_str5) == false : "invalid sku5";
		assert isSKU(not_sku_str6) == false : "invalid sku6";
		System.out.println("isSKU passed \n");
		// if vm and run configuration does not allow assertion, uncomment and run below
		// prints
//    	System.out.print(isSKU(sku_str));
//    	System.out.print("\n");	
//    	System.out.print(isSKU(not_sku_str1));
//    	System.out.print("\n");	
//    	System.out.print(isSKU(not_sku_str2));
//    	System.out.print("\n");	
//    	System.out.print(isSKU(not_sku_str3));
//    	System.out.print("\n");	
//    	System.out.print(isSKU(not_sku_str4));
//    	System.out.print("\n");	

		// test isRound

		System.out.println("testing isRounded function \n");

		String price_str = "231.123";
		String price_str1 = "12.2";
		String isprice_str = "12.32";
		String isprice_str1 = "1.32";
		assert isRounded(price_str) == false : "invalid price";
		assert isRounded(price_str1) == false : "invalid price";
		assert isRounded(isprice_str) == true : "valid price";
		assert isRounded(isprice_str1) == true : "valid price";
		System.out.println("isRounded passed \n");
//    	System.out.print(isRounded(price_str));
//    	System.out.print("\n");	
//    	System.out.print(isRounded(price_str));
//    	System.out.print("\n");

		// test isCustomID
		System.out.println("testing isCustomID function \n");

		String[] invalid_idstrs = { "john@aol...com", "jph123@Klingon.", "jph123@123" };
		String[] valid_idstrs = { "john@aol.com", "jph123@klingon.star", "jph123@live.io" };
		for (int i = 0; i < valid_idstrs.length; i++) {
			String str = valid_idstrs[i];
			assert isCustomerIDEmailAddress(str) == true : "valid id";
//    		System.out.println(i + " i passed \n");
		}
		for (String str : invalid_idstrs) {
			assert isCustomerIDEmailAddress(str) == false : "invalid id";
		}

		System.out.println("isCustomerID passed \n");
		// test isZip
		System.out.println("testing isZip function \n");
		String iszip_str = "95123";
		String iszip_str1 = "12371";
		String iszip_str2 = "95123-3678";
		String notzip_str = "12";
		String notzip_str1 = "123-321";
		String notzip_str2 = "123481";
		String notzip_str3 = "12348-213";
		String notzip_str4 = "12348-21378";
		String notzip_str5 = "12C48";
		assert isZipCode(iszip_str) == true : "valid zip";
		assert isZipCode(iszip_str1) == true : "valid zip";
		assert isZipCode(iszip_str2) == true : "valid zip";
		assert isZipCode(notzip_str) == false : "invalid zip";
		assert isZipCode(notzip_str1) == false : "invalid zip";
		assert isZipCode(notzip_str2) == false : "invalid zip";
		assert isZipCode(notzip_str3) == false : "invalid zip";
		assert isZipCode(notzip_str4) == false : "invalid zip";
		assert isZipCode(notzip_str5) == false : "invalid zip";
		System.out.println("isZip passed \n");

		// test isPropercount
		String cnt_str = "12";
		String notcnt_str = "-12";
		String notcnt_str1 = "1a2";

		assert isProperCount(cnt_str) == true;
		assert isProperCount(notcnt_str) == false;
		assert isProperCount(notcnt_str1) == false;
	}

}
