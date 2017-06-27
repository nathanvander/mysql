package mysql;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import java.sql.SQLException;
import java.util.*;

public class MariaConnector implements Monty {
	public static MARIA_API api;
	//load the libmariadb.dll file
	static {
		api=(MARIA_API)Native.loadLibrary("libmariadb",MARIA_API.class,W32APIOptions.ASCII_OPTIONS);
	}

	public static String get_client_info() {
		return api.mysql_get_client_info();
	}

	/**
	* get a connection. This assumes the port is 3306.
	*/
	public static Connection connect(String host,String user,String passwd,String db) throws SQLException {
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
		return new Maria(m2);
	}

	//===========================================
	public static class Maria extends PointerType implements Connection {
		public Maria(Pointer p) {
			super(p);
		}

		//close() calls mysql_close()
		public void close() {
			api.mysql_close(getPointer());
		}

		//errno() calls mysql_errno()
		public int errno() {
			return api.mysql_errno(getPointer());
		}

		/**
		* execute().  Use this for any SQL command that doesn't contain a SELECT
		* It calls mysql_query().
		*/
		public void execute(String sql) throws SQLException {
			int rc=api.mysql_query(getPointer(),sql);
			if (rc!=0) {
				throw new SQLException("["+rc+"] error executing "+sql);
			}
		}

		/**
		* query(). Use this for an SQL command that does a SELECT or otherwise
		* returns a resultset.
		*/
		public ResultSet query(String sql) throws SQLException {
			int rc=api.mysql_query(getPointer(),sql);
			if (rc!=0) {
				throw new SQLException("["+rc+"] error executing "+sql);
			}
			Pointer r=api.mysql_use_result(getPointer());
			if (r==null) {
				throw new SQLException(sql+ "did not return a result set");
			} else {
				return new MaResultSet(r);
			}
		}

		public long insert_id() {
			return api.mysql_insert_id(getPointer());
		}
	}

	//===========================================
	//When using mysql_use_result(), you must execute mysql_fetch_row() until a NULL value is returned, otherwise,
	//the unfetched rows are returned as part of the result set for your next query.
	public static class MaResultSet extends PointerType implements ResultSet {
		public MaResultSet(Pointer p) {
			super(p);
		}

		//use this when you are done with the result set
		public void free_result() {
			api.mysql_free_result(getPointer());
		}

		//returns the number of fields in the result set
		public int num_fields() {
			return api.mysql_num_fields(getPointer());
		}

		//Retrieves the next row of a result set.  Returns null when there are no more rows to retrieve.
		public String[] fetch_row() {
			return api.mysql_fetch_row(getPointer());
		}

		//there is also fetch_fields, but I think I prefer to do them one at a time
		public Field fetch_field_direct(int fieldno) {
			return api.mysql_fetch_field_direct(getPointer(),fieldno);
		}
	}


	//===========================================
	public interface MARIA_API extends Library {
		public String mysql_get_client_info();
		//returns a pointer to the MYSQL connection
		public Pointer mysql_init(Pointer mysql);
		public Pointer mysql_real_connect(Pointer mysql, String host, String user, String password, String db, int port, String unix_socket, long flags);
		public int mysql_errno(Pointer mysql);
		public void mysql_close(Pointer mysql);
		public int mysql_query(Pointer mysql,String query);
		//MYSQL_RES *mysql_use_result(MYSQL *mysql)
		public Pointer mysql_use_result(Pointer mysql);
		//my_ulonglong mysql_insert_id(MYSQL *mysql)
		public long mysql_insert_id(Pointer mysql);
		//--------------------------------------------
		public void mysql_free_result(Pointer result);
		//unsigned int mysql_num_fields(MYSQL_RES *result)
		public int mysql_num_fields(Pointer result);
		//MYSQL_ROW mysql_fetch_row(MYSQL_RES *result)
		public String[] mysql_fetch_row(Pointer result);
		//MYSQL_FIELD *mysql_fetch_field_direct(MYSQL_RES *result, unsigned int fieldnr)
		public Field mysql_fetch_field_direct(Pointer result,int fieldno);
	}
}