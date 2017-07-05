//no package
import com.sun.jna.PointerType;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.PointerByReference;
import java.util.*;
import java.sql.SQLException;

/**
* This is yet another mysql wrapper based on the php package mysqli.  I am not trying to implement all of the
* mysqli functionality, just the most-commonly used ones.  I use static methods whenever possible.
* I am making everything lower-case to make it more php-ish.
*/
public class mysqli {
	public static mysql_api api;
	//load the libmariadb.dll file
	static {
		api=(mysql_api)Native.loadLibrary("libmariadb",mysql_api.class,W32APIOptions.ASCII_OPTIONS);
	}

	//the con pointer.  This represents a database connection
	public static class con extends PointerType {
		public con(Pointer p) {
			super(p);
		}
	}

	//the result pointer
	public static class result extends PointerType {
		public result(Pointer p) {
			super(p);
		}
	}

	//the field structure.  There are a ton of fields here but we only use the name
	public static class field extends Structure {
		public final static List<String> FIELD_NAMES_LIST = Arrays.asList("name");
		protected List getFieldOrder() {return FIELD_NAMES_LIST;}
		//--------------------------
		public String name;
	}

	//====================================================
	/**
	* get a connection. This assumes the port is 3306.
	* db can be null
	*/
	public static con mysqli_connect(String host,String user,String passwd,String db) throws SQLException  {
		Pointer m = api.mysql_init(null);
		if (m==null) throw new IllegalStateException("pointer m is null");

		//connect to it
		int port=3306;
		Pointer m2 = api.mysql_real_connect(m,host,user,passwd,db,port,null,0);
		if (m2==null) {
			int e = api.mysql_errno(m);
			throw new SQLException("Unable to connect to "+host+" [Error "+e+"]");
		}

		//otherwise we are good to go
		return new con(m2);
	}

	public static void mysqli_close(con c) {
		api.mysql_close(c.getPointer());
	}

	//string mysqli_get_client_info ( mysqli $link )
	//Returns a string that represents the MySQL client library version.
	public static String mysqli_get_client_info () {
		return api.mysql_get_client_info();
	}

	public static boolean mysqli_select_db(con c,String dbname) {
		int rc=api.mysql_select_db(c.getPointer(),dbname);
		if (rc==0) {
			return true;
		} else {
			return false;
		}
	}

	//return a result if it is a select, otherwise return null
	//use mysqli_store_result so we can use num_rows
	public static result mysqli_query(con c,String sql) throws SQLException {
		int rc=api.mysql_query(c.getPointer(),sql);
		if (rc!=0) {
			throw new SQLException("["+rc+"] error executing "+sql);
		}
		Pointer r=api.mysql_store_result(c.getPointer());
		if (r==null) {
			System.out.println("Warning: "+sql+" did not return a result set");
			return null;
		} else {
			return new result(r);
		}
	}

	public static void mysqli_free_result(result r) {
		api.mysql_free_result(r.getPointer());
	}

	//The mysqli_insert_id() function returns the id (generated with AUTO_INCREMENT) used in the last query.
	public static long mysqli_insert_id(con c) {
		return api.mysql_insert_id(c.getPointer());
	}

	//returns null if no more rows
	public static String[] mysqli_fetch_row(result r) {
		Pointer psa=api.mysql_fetch_row(r.getPointer());
		if (psa==null) {
			return null;
		} else {
			int n=mysqli_num_fields(r);
			String[] row=psa.getStringArray(0,n);
			return row;
		}
	}

	//similar to fetch row, but returns a hashmap
	//this has more overhead, so don't use it for multiple rows
	public static HashMap mysqli_fetch_assoc(result r) {
		String[] row=mysqli_fetch_row(r);
		if (row==null) return null;
		else {
			HashMap map=new HashMap();
			for (int i=0;i<row.length;i++) {
				field f=mysqli_fetch_field_direct(r,i);
				map.put(f.name,row[i]);
			}
			return map;
		}
	}

	//mysqli_fetch_array
	//I am not implementing this.  Choose either fetch_row or fetch_assoc.  This is a weird hybrid

	//number of rows in the result set.  Requires the use of mysql_store_result
	public static int mysqli_num_rows(result r) {
		return (int)api.mysql_num_rows(r.getPointer());
	}

	public static int mysqli_num_fields(result r) {
		//returns the number of fields in the result set
		return api.mysql_num_fields(r.getPointer());
	}

	public static int mysqli_errorno(con c) {
		return api.mysql_errno(c.getPointer());
	}

	public static String mysqli_error(con c) {
		return api.mysql_error(c.getPointer());
	}

	public static int mysqli_affected_rows(con c) {
		return (int)api.mysql_affected_rows(c.getPointer());
	}

	public static field mysqli_fetch_field_direct(result r,int fieldno) {
		return api.mysql_fetch_field_direct(r.getPointer(),fieldno);
	}

	public static String mysqli_real_escape_string(con c,String from) {
		//To retrieve a String
		//Pass in an instance of PointerByReference, then use PointerByReference.getValue() to retrieve the "returned" pointer.
		//Pointer.getString(0) will then provide the referenced string.
		if (from==null) {return null;}
		PointerByReference pbr = new PointerByReference();
		api.mysql_real_escape_string(c.getPointer(), pbr, from, from.length());
		Pointer p=pbr.getValue();
		if (p==null) {throw new IllegalStateException("return pointer is null");}
		else {
			return p.getString(0);
		}
	}

	//===================================================
	public interface mysql_api extends Library {
		public String mysql_get_client_info();
		//-----------------------------------------
		//returns a pointer to the MYSQL connection
		public Pointer mysql_init(Pointer mysql);
		public Pointer mysql_real_connect(Pointer mysql, String host, String user, String password, String db, int port, String unix_socket, long flags);
		public int mysql_errno(Pointer mysql);
		public String mysql_error(Pointer mysql);
		public void mysql_close(Pointer mysql);
		//int mysql_select_db(MYSQL *mysql, const char *db)
		public int mysql_select_db(Pointer mysql,String db);
		public int mysql_query(Pointer mysql,String query);
		//MYSQL_RES *mysql_use_result(MYSQL *mysql)
		public Pointer mysql_use_result(Pointer mysql);
		//MYSQL_RES *mysql_store_result(MYSQL *mysql)
		public Pointer mysql_store_result(Pointer mysql);
		//my_ulonglong mysql_insert_id(MYSQL *mysql)
		public long mysql_insert_id(Pointer mysql);
		//my_ulonglong mysql_affected_rows(MYSQL *mysql)
		public long mysql_affected_rows(Pointer mysql);
		//--------------------------------------------
		public void mysql_free_result(Pointer result);
		//unsigned int mysql_num_fields(MYSQL_RES *result)
		public int mysql_num_fields(Pointer result);
		//my_ulonglong mysql_num_rows(MYSQL_RES *result)
		public long mysql_num_rows(Pointer result);

		//MYSQL_ROW mysql_fetch_row(MYSQL_RES *result)
		//this is a pointer to a string array
		public Pointer mysql_fetch_row(Pointer result);
		//MYSQL_FIELD *mysql_fetch_field_direct(MYSQL_RES *result, unsigned int fieldnr)
		public field mysql_fetch_field_direct(Pointer result,int fieldno);
		//------------------------------------------
		//unsigned long mysql_real_escape_string(MYSQL *mysql, char *to, const char *from, unsigned long length)
		public long mysql_real_escape_string(Pointer mysql, PointerByReference to, String from, long len);
	}
}