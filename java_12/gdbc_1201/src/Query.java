import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Query {
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                // 创建数据库连接
                connection = DBUtil.getConnection2();
                // 根据数据库连接创建操作命令对象
                statement = connection.createStatement();
                String sql = "select id,name,chinese,math,english from exam_result";
                // 操作命令对象执行sql语句，返回结果集
                // List<Map<String, Object>>
                resultSet = statement.executeQuery(sql);
                while(resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    BigDecimal chinese = resultSet.getBigDecimal("chinese");
                    BigDecimal math = resultSet.getBigDecimal("math");
                    BigDecimal english = resultSet.getBigDecimal("english");
                    System.out.printf("id=%s, name=%s, chinese=%s,math=%s,english=%s",
                            id,name,chinese,math,english);
                    System.out.println();
                }
            } finally {
                DBUtil.close(connection,statement,resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
