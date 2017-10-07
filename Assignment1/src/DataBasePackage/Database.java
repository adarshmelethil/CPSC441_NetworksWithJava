/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataBasePackage;

import DataStructure.Data;
import NetworkConnection.TCPClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final String M_DATA_FOLDER = "Data";
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
//        id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
//        host_name TEXT NOT NULL,
//        port_num INT DEFAULT 80);

//        CREATE TABLE Query(
//        id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
//        accept_ranges TEXT,
//        server TEXT,
//        etag TEXT,
//        last_modified DATETIME,
//        date DATETIME,
//        content_length INT,
//        content_type TEXT,
//        file_path TEXT NOT NULL,
//        host_id INTEGER REFERENCES Hosts(id) ON DELETE CASCADE); 

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
    
    public int getHostID(String url, int host_num){
        String sql_statement_host = "SELECT id FROM Hosts WHERE host_name=? AND port_num=?";
        
        try{
            PreparedStatement statement = m_db_connection.prepareStatement(sql_statement_host);
            statement.setString(1, url);
            statement.setInt(2, host_num);
            ResultSet result = statement.executeQuery();
            int id = -1;
            while (result.next()){
                if (id < 0){
                id = result.getInt("id");
                }else {
                    System.out.println("Found more than one");
                }
            }
            return id;
        }catch(SQLException ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    public boolean queryExists(int host_id, String data_path){
        String sql_statement_host = "SELECT id FROM Hosts WHERE host_name=? AND port_num=?";
        
        try{
            PreparedStatement statement = m_db_connection.prepareStatement(sql_statement_host);
            statement.setString(1, url);
            statement.setInt(2, host_num);
            ResultSet result = statement.executeQuery();
            int id = -1;
            while (result.next()){
                if (id < 0){
                id = result.getInt("id");
                }else {
                    System.out.println("Found more than one");
                }
            }
            return id;
        }catch(SQLException ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    public void insertHost(String url, int host_num){
        String sql_statement_host = "INSERT INTO Hosts(host_name, port_num) VALUES(?,?)";
        
        try {
            PreparedStatement statement = m_db_connection.prepareStatement(sql_statement_host);
            statement.setString(1, url);
            statement.setInt(2, host_num);
            statement.executeUpdate();
        }catch(SQLException ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    
    public void createDirectories(String folder_name){
        File theDir = new File(folder_name);
//        System.out.println("checking directory");
        // if the directory does not exist, create it
        if (!theDir.exists()) {
//            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdirs();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }
            if(result) {    
//                System.out.println("DIR created");  
            }
        }
    }
    
    public void saveDataToFile(ArrayList<byte[]> data, String file_path, String content_name){
        FileOutputStream fos;
        String data_path = file_path + "/" + content_name;
        try {
            fos = new FileOutputStream(data_path);
            for(byte[] line_of_data : data){
                fos.write(line_of_data);
            }
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int readLineFromInputStream(BufferedInputStream input_stream, byte[] response_buffer, int total_length){
        int index = 0;
        try{
            index = 0;
            while(true){
                int read_byte = input_stream.read();
                if(read_byte < 0){
                    break;
                }
                response_buffer[index++] = (byte)read_byte;
                if((char)read_byte == '\n'){
                    break;
                }
                if(index >= total_length){
                    System.out.println("Ran out of space");
                    break;
                }
            }
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, "Stream Closed", ex);
            return -1;
        }
        return index;
    }
    
    public ArrayList<byte[]> loadDataFromFile(String data_path){
        ArrayList<byte[]> total_data = new ArrayList();
        File file;
        BufferedInputStream file_input_stream;
        
        try {
            file = new File(data_path);
            if (file.exists()){
                System.out.println("File:");
                System.out.println(file);
                file_input_stream = new BufferedInputStream(new FileInputStream(file));

                byte[] line_buffer = new byte[1000];
                int bytes_read;

                while(true){
                    bytes_read = readLineFromInputStream(file_input_stream, line_buffer, 1000);
                    if(bytes_read < 1) break;
                    byte[] data_line = Arrays.copyOf(line_buffer, bytes_read);
                    total_data.add(data_line);
                }
            }else{
                System.out.println("File does not exist");
            }

        }catch (IOException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return total_data;
    }
    
    public void insert(Data data){
        int id = getHostID(data.getHostName(), data.getPortNum());
        if(id < 0){
            insertHost(data.getHostName(), data.getPortNum());
            id = getHostID(data.getHostName(), data.getPortNum());
        }
        System.out.println(id);
        
        String hostname_portnum = data.getHostName() + "_" + data.getPortNum();
        String url_path = data.getQuery();
        int file_content_split_index = url_path.lastIndexOf("/");
        String folder_path = M_DATA_FOLDER +"/"+ hostname_portnum + url_path.substring(0, file_content_split_index);
        String content = url_path.substring(file_content_split_index+1);
        if(content.length() == 0) content = "LandingPage";
        
        
        createDirectories(folder_path);
        saveDataToFile(data.getData(), folder_path, content);
        
        
//        String[] folders = data.getQuery().split("/");
//        System.out.println(folder_path);
//        System.out.println("-" + content);
//        ArrayList<String> available_keys = availableQueryKey(data.getHeader());
//        String sql_statement_query = createQueryStatement(available_keys);
         
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
//        Data data = client.makeRequest("people.ucalgary.ca");
        System.out.println(data);
//        db.insert(data);
        //        System.out.println(db.getHostID(data.getHostName(), data.getPortNum()));
//        System.out.println(db.getHostID(data.getHostName(), 81));
        

// LOADINg TEST
//        System.out.println("loading");
//        ArrayList<byte[]> loaded = db.loadDataFromFile("Data/people.ucalgary.ca_80/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
//        System.out.println("done loading");
//        
//        System.out.println(loaded.size());
//        for(byte[] bb : loaded){
//            for(byte b : bb){
//                System.out.print((char)b);
//            }
//        }
        
        
    }
    
}
