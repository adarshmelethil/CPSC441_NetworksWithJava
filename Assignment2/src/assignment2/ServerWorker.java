/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author cow
 */
public class ServerWorker extends Thread{
    Socket m_socket;
    
    /*
     * Default Constructor
     */
    ServerWorker(Socket socket) throws IOException{
        m_socket = socket;
    }
    
    private byte[] getFile(File file){
        
        if(!file.exists()){
            return null;
        }
        byte[] file_bytes = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(file_bytes);
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, e);
        }catch (IOException e1) {
            System.out.println("Error Reading The File.");
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, e1);
        }
        return file_bytes;
    }
    
    private HashMap<String, String> processHeader(String request){
        HashMap<String, String> header_values = new HashMap();
        String[] request_lines = request.split("\r\n");
        for(String line: request_lines){
            int index_of_split = line.indexOf(":");
            if(index_of_split < 0){
                index_of_split = line.indexOf(" ");
            }
            String key = line.substring(0, index_of_split).trim();
            String value = line.substring(index_of_split+1).trim();
            
            header_values.put(key, value);
        }
        return header_values;
    }
    
    private String getPathFromGetRequest(String value){
        int index_of_space = value.indexOf(" ");
        String file_path = value.substring(0, index_of_space).trim();
        String type = value.substring(index_of_space).trim();
        String[] type_split = type.split("/");
        if(type_split.length==2 && type_split[0].equals("HTTP") && type_split[1].equals("1.1")){
            file_path = file_path.replaceAll("^/|/$", "");
            if(file_path.length() == 0){
                file_path = "index.html";
            }
        }else{
            return null;
        }
        return file_path;
    }
    
    private String getFileType(File file){
        Path source = Paths.get(file.getAbsolutePath());
        String file_type = "text/plain";
        try {
            file_type = Files.probeContentType(source);
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file_type;
    }
    
    private String getCurrentGMTTime(){
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(currentTime);
    }
    
    private String getLastModified(File file){
        Date currentTime = new Date(file.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(currentTime);
    }
    
    private byte[] processRequest(String request){
        String header = "";
        String content_info = "";
        String data = "";
        byte[] file_bytes = new byte[0];
        HashMap<String, String> header_values = processHeader(request);
        String get_value = header_values.get("GET");
        if(get_value != null){
            String file_path = getPathFromGetRequest(get_value);
            if(file_path == null){
                header = "HTTP/1.1 400 Bad Request\r\n";
            }else{
                File file = new File(file_path);
                file_bytes = getFile(file);
                if(file_bytes == null){
                    header = "HTTP/1.1 404 Not Found\r\n";
                    file_bytes = new byte[0];
                }else{
                    header = "HTTP/1.1 200 OK\r\n";
                    content_info = "Content-Length: " + file_bytes.length + "\r\n"
                                 + "Content-Type: " + getFileType(file) + "\r\n"
                                 + "Last-Modified: " + getLastModified(file) + "\r\n";
                }
            }
        }else{
            header = "HTTP/1.1 400 Bad Request\r\n";
        }
        header += "Server: Java Application\r\n"
                + "Date: " + getCurrentGMTTime() + "\r\n"
                + content_info 
                + "Connection : close\r\n"
                + "\r\n";
        
        ByteArrayOutputStream response = new ByteArrayOutputStream( );
        try {
            response.write( header.getBytes() );
            response.write( file_bytes );
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return response.toByteArray();
    }
    /*
     * Thread run override
     */
    @Override
    public void run(){
        try {
            BufferedReader input_request = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            OutputStream output_response = m_socket.getOutputStream();
            System.out.println("here");
            char[] input_buffer = new char[10000];
            
            int read_value = -1;
            if(input_request.ready()){
                read_value = input_request.read(input_buffer);
                System.out.println(read_value);

            }
            if(read_value <= 0 ){
                return;
            }
            String read_line = String.valueOf(input_buffer, 0, read_value);
            byte[] response = processRequest(read_line);
            output_response.write(response);
            output_response.flush();
            input_request.close();
            output_response.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Worker over");
    }
}
