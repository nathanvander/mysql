package mysql;
import java.sql.SQLException;

public class Test {
	public static void main(String[] args) throws SQLException{
		String host="localhost";
		Monty.Connection mc=MariaConnector.connect(host,"root","root","test");
		String sql="DROP TABLE IF EXISTS PERSON";
		mc.execute(sql);
		String sql2="CREATE TABLE IF NOT EXISTS PERSON ";
		sql2+="( ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,";
		sql2+=" FIRST_NAME VARCHAR(20),";
		sql2+=" LAST_NAME VARCHAR(20))";
		mc.execute(sql2);
    	mc.close();
	}
}