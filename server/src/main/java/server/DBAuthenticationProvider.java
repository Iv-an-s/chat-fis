package server;

import java.sql.*;

public class DBAuthenticationProvider implements AuthenticationProvider {
//    пока серверу база данных не нужна, мы можем соединение с базой данных открыть только здесь. Снаружи не открывать.
    private DbConnection dbConnection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    @Override
    public void init() {
        dbConnection = new DbConnection();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String query = String.format("select nickname from users where login = '%s' and password = '%s';", login, password);
        try (ResultSet rs = dbConnection.getStmt().executeQuery(query)){
            if(rs.next()){
                return rs.getString("nickname");
            }

        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void changeNickname(String oldNickname, String newNickname) {
        String query = String.format("update users set nickname = '%s' where nickname = '%s';", newNickname, oldNickname);
        try {
            dbConnection.getStmt().executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNickBusy(String nickname) {
        String query = String.format("select id from users where nickname = '%s';", nickname);
        ResultSet rs = null;
        try {
            rs = dbConnection.getStmt().executeQuery(query);
            if (rs.next()){
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    @Override
    public void shutdown() {
        dbConnection.close();
    }
}
