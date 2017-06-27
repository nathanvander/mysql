package mysql;
import java.sql.SQLException;

public class Test2 {
	public static void main(String[] args) throws SQLException{
		String host="localhost";
		Monty.Connection mc=MariaConnector.connect(host,"root","root","test");
		String sql="INSERT INTO PERSON (FIRST_NAME,LAST_NAME) VALUES('John','Doe')";
		mc.execute(sql);

		String sql2="SELECT * FROM PERSON";
		Monty.ResultSet rs=mc.query(sql2);
		//int n=rs.num_fields();
		//System.out.println("there are "+n+" fields");


		int nf=rs.num_fields();
		for (int i=0;i<nf;i++) {
			Monty.Field f=rs.fetch_field_direct(i);
			System.out.println("field name: "+f.name);
			//System.out.println(f.org_name);
			//System.out.println(f.table);
			//System.out.println(f.org_table);
			//System.out.println(f.db);
			//System.out.println(f.catalog);
			//System.out.println(f.def);
			//System.out.println("field length: "+f.length);
			//System.out.println("field max length: "+f.max_length);
			//System.out.println("field type: "+f.type);
			//System.out.println("field value: "+row[i]);
		}

		String[] row=rs.fetch_row();
		System.out.println(row[0]+":"+row[1]);

		rs.free_result();
		mc.close();
	}
}
