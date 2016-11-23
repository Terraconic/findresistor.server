package com.company;

import java.sql.*;


public class DataBase {

    private static DataBase dataBase;

    private static final String url = "jdbc:mariadb://localhost:3306/DB_users";
    private static final String user = "localhost";
    private static final String password = "password";

    private static Connection connection;
    private static ResultSet result;
    private static boolean initialization = false;

    private DataBase() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Data Base: connection success");
        } catch (SQLException ex) {
            System.out.println("Data Base error: error connection");
            ex.printStackTrace();
        }
    }

    public static void InitDB() {
        if (!initialization) {
            dataBase = new DataBase();
            initialization = true;
        }
    }


    public static String[] getUserName(String mail) {

        System.out.println("Data Base: Start get user name");

        String user[] = {null, null, null};

        try {
            PreparedStatement preparedSelect = connection.prepareStatement(
                    "SELECT user_name, salt, hash_pass FROM users WHERE user_name = ?");
            preparedSelect.setString(1, mail);

            result = preparedSelect.executeQuery();

            while (result.next()) {
                user[0] = result.getString(1);
                user[1] = result.getString(2);
                user[2] = result.getString(3);
            }

        } catch (SQLException | NullPointerException  ex) {
            System.out.println("Data Base: User not found");
            return user;
        }

        //System.out.println("***End get user name***");
        return user;
    }

    public static boolean insertUser(String name, String salt, String hashPass) {

        System.out.println("Data Base: Start insert user");

        String user[] = getUserName(name);

        try {
            if (user[0] == null) {
                try {
                    PreparedStatement preparedInsert = connection.prepareStatement(
                            "INSERT INTO users (user_name, salt, hash_pass) VALUES (?, ?, ?)");

                    preparedInsert.setString(1, name);
                    preparedInsert.setString(2, salt);
                    preparedInsert.setString(3, hashPass);

                    preparedInsert.executeUpdate();

                    System.out.println("Data Base: End insert user");
                    return true;

                } catch (SQLException ex) {
                    System.out.println("Data Base error: error insert");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("Data Base error: error");
            return false;
        }
        return false;
    }

    public static boolean isInitialization() {
        return initialization;
    }

    public static void closeDataBase() {

        try {
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
