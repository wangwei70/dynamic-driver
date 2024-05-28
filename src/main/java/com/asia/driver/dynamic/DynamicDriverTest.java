package com.asia.driver.dynamic;

import com.asia.driver.dynamic.loader.DriverClassLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class DynamicDriverTest {
    final static String mysqlJdbcFile = "D:\\IdeaProjects\\dynamicdriver\\src\\main\\resources\\driver\\postgresql\\42_2_26";

    final static HashMap<String,String> map=new HashMap<String,String>();
    static {
//        map.put();
    }
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {

//        URL u = new URL("file:D:\\IdeaProjects\\dynamicdriver\\target\\classes\\postgresql-42.1.1.jar");

        Properties properties = new Properties();
        properties.put("user", "wangwei");
        properties.put("password", "wangwei");


        testConn("file:D:\\IdeaProjects\\driver_test\\lib\\driver\\postgresql\\42_1_1\\postgresql-42.1.1.jar", properties);
        testConn("file:D:\\IdeaProjects\\driver_test\\lib\\driver\\postgresql\\42_1_1\\postgresql-42.1.1.jar", properties);


//        Driver d = (Driver)Class.forName("Driver").newInstance();
//        Properties properties = new Properties();
//        properties.put("user", "wangwei");
//        properties.put("password", "wangwei");
//        Connection con = d.connect("jdbc:postgresql://10.1.206.136:8402/postgres?useUnicode=true", properties);
//        Statement statement = con.createStatement();
//        ResultSet resultSet = statement.executeQuery("select * from acc_config");
//        while (resultSet.next()) {
//            System.out.println(resultSet.getString(2));
//        }


//        Properties properties = new Properties();
//        properties.put("user", "wangwei");
//        properties.put("password", "wangwei");
//        Driver driver = new Driver();
//        String url = "jdbc:postgresql://10.1.206.136:8402/postgres?useUnicode=true";
//        Connection con = driver.connect(url, properties);
//        Statement statement = con.createStatement();
//        ResultSet resultSet = statement.executeQuery("select * from acc_config");
//        while (resultSet.next()) {
//            System.out.println(resultSet.getString(2));
//        }
    }

    private static void testConn(String jar,Properties properties) throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
//        URL url = new URL("jar:file:"+jar+"!/");
        URL url = new URL(jar);
        DriverClassLoader ucl = new DriverClassLoader(new URL[]{url});
        Class<?> driverClass = ucl.loadClass("com.asia.driver.dynamic.Driver");
        java.sql.Driver driver = (java.sql.Driver) driverClass.newInstance();
        System.out.println("主版本："+driver.getMajorVersion()+",次版本："+driver.getMinorVersion());
        Connection connection = driver.connect("jdbc:postgresql://10.1.206.136:8402/postgres?useUnicode=true", properties);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from acc_config");
        while (resultSet.next()) {
            System.out.println(resultSet.getString(2));
        }
    }
}
