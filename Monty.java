package mysql;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import java.sql.SQLException;
import java.util.*;

/**
* This defines the interfaces for mysql.  Mariadb is one of the implementations.
*
* I call this Monty after the author of MySql and MariaDB.  This just has 2 main interfaces defined: Connection and ResultSet
*/

public interface Monty {

	/**
	* Returns a string that represents the client library version
	*/
	public static String get_client_info() {
		//override this, obviously
		return null;
	}

	/**
	* get a connection. This assumes the port is 3306.
	*/
	public static Connection connect(String host,String user,String passwd,String db) throws SQLException {
		//override this, obviously
		return null;
	}


	//==========================================
	public static interface Connection {

		//close() calls mysql_close()
		public void close();

		//errno() calls mysql_errno()
		public int errno();

		//use START TRANSACTION, COMMIT, and ROLLBACK instead of making more methods for them

		/**
		* execute().  Use this for any SQL command that doesn't contain a SELECT
		* It calls mysql_query().
		*/
		public void execute(String sql) throws SQLException;

		/**
		* query(). Use this for an SQL command that does a SELECT or otherwise
		* returns a resultset.
		*/
		public ResultSet query(String sql) throws SQLException;

		public long insert_id();
	}

	//===========================================
	public static interface ResultSet {

		//use this when you are done with the result set
		public void free_result();

		//returns the number of fields in the result set
		public int num_fields();

		//Retrieves the next row of a result set.  Returns null when there are no more rows to retrieve.
		public String[] fetch_row();

		//there is also fetch_fields, but I think I prefer to do them one at a time
		public Field fetch_field_direct(int fieldno);
	}

	//===========================================
	//this is metadata about a field
	//from mysql.h
	//  typedef struct st_mysql_field {
	//    char *name;			/* Name of column */
	//    char *org_name;		/* Name of original column (added after 3.23.58) */
	//    char *table;			/* Table of column if column was a field */
	//    char *org_table;		/* Name of original table (added after 3.23.58 */
	//    char *db;                     /* table schema (added after 3.23.58) */
	//    char *catalog;                /* table catalog (added after 3.23.58) */
	//    char *def;			/* Default value (set by mysql_list_fields) */
	//    unsigned long length;		/* Width of column */
	//    unsigned long max_length;	/* Max width of selected set */
	//  /* added after 3.23.58 */
	//    unsigned int name_length;
	//    unsigned int org_name_length;
	//    unsigned int table_length;
	//    unsigned int org_table_length;
	//    unsigned int db_length;
	//    unsigned int catalog_length;
	//    unsigned int def_length;
	//  /***********************/
	//    unsigned int flags;		/* Div flags */
	//    unsigned int decimals;	/* Number of decimals in field */
	//    unsigned int charsetnr;       /* char set number (added in 4.1) */
	//    enum enum_field_types type;	/* Type of field. Se mysql_com.h for types */
	//    void *extension;              /* added in 4.1 */
  	//}MYSQL_FIELD;

	public static class Field extends Structure {
		public final static String[] FIELD_NAMES = {"name","org_name","table","org_table","db","catalog","def","length","max_length","name_length","org_name_length",
			"table_length","org_table_length","db_length","catalog_length","def_length","flags","decimals","charsetnr","type","extension"};
		public final static List<String> FIELD_NAMES_LIST = Arrays.asList(FIELD_NAMES);
		protected List getFieldOrder() {return FIELD_NAMES_LIST;}
		//--------------------------
		public String name;
		public String org_name;
		public String table;
		public String org_table;
		public String db;
		public String catalog;
		public String def;
		public long length;
		public long max_length;
		public int name_length;
		public int org_name_length;
		public int table_length;
		public int org_table_length;
		public int db_length;
		public int catalog_length;
		public int def_length;
		public int flags;
		public int decimals;
		public int charsetnr;
		public enum_field_types type;
		public Object extension;	//this is a Pointer but we don't want to use it
	}

	//===============================================
	//adapted from mysql_com.h
	public static interface enum_field_types {
		public static final int MYSQL_TYPE_DECIMAL = 0;
		public static final int MYSQL_TYPE_TINY = 1;
        public static final int MYSQL_TYPE_SHORT = 2;
        public static final int MYSQL_TYPE_LONG = 3;
        public static final int MYSQL_TYPE_FLOAT = 4;
        public static final int MYSQL_TYPE_DOUBLE = 5;
        public static final int MYSQL_TYPE_NULL = 6;
        public static final int MYSQL_TYPE_TIMESTAMP= 7;
        public static final int MYSQL_TYPE_LONGLONG=8;
        public static final int MYSQL_TYPE_INT24=9;
        public static final int MYSQL_TYPE_DATE=10;
        public static final int MYSQL_TYPE_TIME=11;
        public static final int MYSQL_TYPE_DATETIME=12;
        public static final int MYSQL_TYPE_YEAR=13;
        public static final int MYSQL_TYPE_NEWDATE=14;
        public static final int MYSQL_TYPE_VARCHAR=15;
        public static final int MYSQL_TYPE_BIT=16;
        public static final int MYSQL_TYPE_NEWDECIMAL=246;
        public static final int MYSQL_TYPE_ENUM=247;
        public static final int MYSQL_TYPE_SET=248;
        public static final int MYSQL_TYPE_TINY_BLOB=249;
        public static final int MYSQL_TYPE_MEDIUM_BLOB=250;
        public static final int MYSQL_TYPE_LONG_BLOB=251;
        public static final int MYSQL_TYPE_BLOB=252;
        public static final int MYSQL_TYPE_VAR_STRING=253;
        public static final int MYSQL_TYPE_STRING=254;
        public static final int MYSQL_TYPE_GEOMETRY=255;
	}

}