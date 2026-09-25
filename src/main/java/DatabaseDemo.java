import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseDemo {

    public static void main(String[] args) {
        String url = "jdbc:h2:file:./data/movier";
        String username = "sa";
        String password = "";

        try (Connection connection =
                     DriverManager.getConnection(url, username, password)) {

            System.out.println("H2 数据库连接成功！");

            InputStream sqlFile =
                    DatabaseDemo.class.getResourceAsStream("/schema.sql");

            if (sqlFile == null) {
                throw new IllegalStateException("找不到 schema.sql");
            }

            try (InputStreamReader reader =
                         new InputStreamReader(sqlFile, StandardCharsets.UTF_8)) {

                RunScript.execute(connection, reader);
            }

            System.out.println("movies 表创建成功！");
            long tmdbId = 129;

            String existsSql = "SELECT COUNT(*) FROM movies WHERE tmdb_id = ?";
            boolean movieExists;

            try (PreparedStatement existsStatement =
                         connection.prepareStatement(existsSql)) {

                existsStatement.setLong(1, tmdbId);

                try (ResultSet resultSet = existsStatement.executeQuery()) {
                    resultSet.next();
                    movieExists = resultSet.getInt(1) > 0;
                }
            }

            if (!movieExists) {
                String insertSql = """
            INSERT INTO movies
            (tmdb_id, title, original_title, release_year, popularity, poster_path)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSql)) {

                    insertStatement.setLong(1, tmdbId);
                    insertStatement.setString(2, "千与千寻");
                    insertStatement.setString(3, "千と千尋の神隠し");
                    insertStatement.setInt(4, 2001);
                    insertStatement.setDouble(5, 92.4);
                    insertStatement.setString(6, "/spirited-away.jpg");

                    int insertedRows = insertStatement.executeUpdate();
                    System.out.println("成功插入 " + insertedRows + " 条电影数据");
                }
            } else {
                System.out.println("电影已经存在，不再重复插入");
            }
            String selectSql = "SELECT * FROM movies";

            try (PreparedStatement selectStatement =
                         connection.prepareStatement(selectSql);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    System.out.println("数据库内部编号：" + resultSet.getLong("id"));
                    System.out.println("TMDB编号：" + resultSet.getLong("tmdb_id"));
                    System.out.println("电影名称：" + resultSet.getString("title"));
                    System.out.println("原始名称：" + resultSet.getString("original_title"));
                    System.out.println("上映年份：" + resultSet.getInt("release_year"));
                    System.out.println("热度：" + resultSet.getDouble("popularity"));
                    System.out.println("海报路径：" + resultSet.getString("poster_path"));
                }
            }

        } catch (SQLException e) {
            System.out.println("数据库操作失败！");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("读取 schema.sql 失败！");
            e.printStackTrace();
        }
    }
}