package NetworkConnection;

import DataStructure.Data;
import DataStructure.URL;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient{
    // Response FROM server
    PrintWriter m_output_stream;
    // Request stream TO server
    BufferedInputStream m_response_stream;
    
    public Data makeRequest(String url_name){
        Data data = new Data();
        data.newURL(url_name);
//        System.out.println(data.currentURL);
        
        openConnection(data.getHostName(), data.getPortNum());
        sendMessage(data.getHostName(), data.getQuery());
        data.setResponseHeader(readResponseHeader());
        data.setData(readResponseData(data.getContentLength()));
        closeConnection();
        
        return data;
    }
    
    public Data makeRequest(URL url){
        Data data = new Data();
        data.setURL(url);
//        System.out.println(data.currentURL);
        
        openConnection(data.getHostName(), data.getPortNum());
        sendMessage(data.getHostName(), data.getQuery());
        data.setResponseHeader(readResponseHeader());
        data.setData(readResponseData(data.getContentLength()));
        closeConnection();
        
        return data;
    }
    
    public Data makeConditionalRequest(URL url, String date){
        Data data = new Data();
        data.setURL(url);
//        System.out.println(data.currentURL);
        
        openConnection(data.getHostName(), data.getPortNum());
        sendConditionalMessage(data.getHostName(), data.getQuery(), date);
        data.setResponseHeader(readResponseHeader());
        data.setData(readResponseData(data.getContentLength()));
        closeConnection();
        
        return data;
    }
    
    private void openConnection(String host_name, int port_number){
//        System.out.println("OPENING CONNECTION TO: " + host_name);
        try {
            Socket socket;
            socket = new Socket(host_name, port_number);
        
            m_output_stream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
            m_response_stream = new BufferedInputStream((socket.getInputStream()));

        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMessage(String host_name, String query){
//        System.out.println("SENDING MESSAGE:");
        String sent_message = 
                    "GET " + query + " HTTP/1.1\r\n"
                +   "Host: " + host_name + "\r\n"
                +   "\r\n";
        m_output_stream.print(sent_message);
//        System.out.print(sent_message);
        m_output_stream.flush();
    }
    
    public void sendConditionalMessage(String host_name, String query, String date){
        String sent_message = 
                    "GET " + query + " HTTP/1.1\r\n"
                +   "Host: " + host_name + "\r\n"
                +   "Last-Modified: " + date + "\r\n"
                +   "\r\n";
        m_output_stream.print(sent_message);
//        System.out.print(sent_message);
        m_output_stream.flush();
    }
    
    // TA:minh.nguyen5@ucalgary.ca
    private int readLineFromInputStream(byte[] response_buffer, int total_length){        int index = 0;
        try{
            index = 0;
            while(true){
                int read_byte = m_response_stream.read();
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
    
    private HashMap<String, String> readResponseHeader(){
//        System.out.println("READING RESPONSE HEADER:");
        HashMap<String, String> response_header = new HashMap(); 
        
        byte[] response_buffer = new byte[1000];
        int bytes_read = readLineFromInputStream(response_buffer, 1000);
        String message = new String(response_buffer, 0, bytes_read);
        response_header.put("Status", message);
//        System.out.println(message);
        
        while(true){
            bytes_read = readLineFromInputStream(response_buffer, 1000);
            message = new String(response_buffer, 0, bytes_read);
//            System.out.println(message.length() + "-'" + message +"'");
            if(message.length()-2 == 0) break;
            String[] key_value_pair = message.split("[:]");
            response_header.put(key_value_pair[0].replaceAll("\\s+",""), key_value_pair[1].replaceAll("\\s+",""));
        }
  
        return response_header;
    }
    
    private ArrayList<byte[]> readResponseData(int content_length){
//        System.out.println("READING RESPONSE DATA:");
        ArrayList<byte[]> total_data = new ArrayList();
        int total_bytes_read = 0;
        
        byte[] response_buffer = new byte[1000];
        int bytes_read;
        while(true){
            bytes_read = readLineFromInputStream(response_buffer, 1000);
            total_bytes_read += bytes_read;
//            String message = new String(response_buffer, 0, bytes_read);
//            System.out.print(total_bytes_read + "-'" + message + "'");
                
            byte[] data = Arrays.copyOf(response_buffer, bytes_read);
            total_data.add(data);
            
            if(total_bytes_read >= content_length) break;
        }
        return total_data;
    }
    
    private void closeConnection(){
//        System.out.println("CLOSING CONNECTION");
        try{
            m_response_stream.close();
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_output_stream.close();
    }
    
    public static void main(String[] args){
        TCPClient tcpClient = new TCPClient();
        Data data;
        
        String[] test_urls = {
            "people.ucalgary.ca",
            "people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp",
            "people.ucalgary.ca/~mghaderi/test/uc.gif"
        };
        
        for(String url: test_urls){
            data = tcpClient.makeRequest(url);
//            System.out.println(data);
            System.out.print(data.getHeaderValue("Date"));
            System.out.println("---");
        }
//        data = tcpClient.makeRequest("people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
//        System.out.print(data.getHeader("Date"));
    }
}