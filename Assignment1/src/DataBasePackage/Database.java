/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataBasePackage;

import DataStructure.Data;
import NetworkConnection.TCPClient;
import java.sql.*;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author cow
 */
public class Database {
    Connection m_db_connection = null;
    public static final String M_DATABASE_URL = "jdbc:sqlite:cpsc_a1.db";
    private static final String[] M_QUERY_COLUMNS = {
        "accept_ranges",
        "server",
        "etag",
        "last_modified",
        "date",
        "content_length",
        "content_type",
    };
    private static Connection ConnectDB(){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(Database.M_DATABASE_URL);
            
            System.out.println("Connected to Database");
            
            return conn;
        }catch(Exception ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    public Database(){
        m_db_connection = ConnectDB();
//        CREATE TABLE Hosts(
//        id INT PRIMARY KEY ON CONFLICT ABORT NOT NULL UNIQUE,
//        host_name TEXT NOT NULL,
//        port_num INT DEFAULT 80);

//        CREATE TABLE Query(
//        id INTEGER PRIMARY KEY AUTOINCREMENT,
//        accept_ranges TEXT,
//        server TEXT,
//        etag TEXT,
//        last_modified DATETIME NOT NULL,
//        date DATETIME NOT NULL,
//        content_length INT NOT NULL,
//        content_type TEXT NOT NULL,
//        host_id INT REFERENCES Hosts(id) ON DELETE CASCADE, 
//        file_path TEXT NOT NULL DEFAULT '/',
//        FOREIGN KEY(host_id)REFERENCES Hosts(id) ON DELETE CASCADE);

    }
    
    public String serverkeyToDBkey(String key){
        String formated_key = key.toLowerCase();
        formated_key = formated_key.replace("-", "_");
        return formated_key;
    }
    
    public ArrayList<String> availableQueryKey(HashMap<String, String> header){
        Set<String> header_keys = header.keySet();
        ArrayList<String> keys_to_store = new ArrayList();
        List<String> stored_query_keys = Arrays.asList( M_QUERY_COLUMNS);
        for(String possible_key : header_keys){
            
            if(stored_query_keys.contains(serverkeyToDBkey(possible_key))){
                keys_to_store.add(possible_key);
            }
        }
        return keys_to_store;
    }
    
    public String createQueryStatement(ArrayList<String> storing_keys){
        StringJoiner query_content = new StringJoiner(",");
        StringJoiner query_value_placeholder = new StringJoiner(",");

        for(String key: storing_keys){
            String db_key = serverkeyToDBkey(key);
            query_content.add(db_key);
            query_value_placeholder.add("?");
        }
        
        String sql_statement_query = "INSERT INTO Query("+query_content.toString()+") VALUES(" + query_value_placeholder.toString() + ")";
        return sql_statement_query;
    }
    
    public int insertHost(String url, int host_hum){
        String sql_statement_host = "INSERT INTO Hosts(host_name, port_num) VALUES(?,?)";
        
        return -1;
    }
    
    public void insert(Data data){
        insertHost(data.getHostName(), data.getPortNum());
        
        ArrayList<String> available_keys;
        available_keys = availableQueryKey(data.getHeader());
        String sql_statement_query = createQueryStatement(available_keys);
        
    }
    
    public void closeConnection(){
        try {
            if (m_db_connection != null) {
                m_db_connection.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[]){
        Database db = new Database();
        TCPClient client = new TCPClient();
        
        Data data = client.makeRequest("people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
    }
    
}
