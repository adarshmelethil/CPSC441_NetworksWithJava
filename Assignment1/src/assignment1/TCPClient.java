package assignment1;

import DataStructure.Data;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
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
    BufferedReader m_inputStream;
    BufferedInputStream m_response_stream;
    
    public void makeRequest(String url_name){
        Data data = new Data();
        data.newURL(url_name);
//        System.out.println(data.currentURL);
        
        openConnection(data.getHostName());
        sendMessage(data.getHostName(), data.getQuery());
        data.setResponseHeader(readResponseHeader());
        data.setData(readResponseData(data.getContentLength()));
        closeConnection();
    }
    
    private void openConnection(String host_name){
        System.out.println("Opening Connection...");
        try {
            Socket socket;
            socket = new Socket(host_name, 80);
        
            m_output_stream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
            m_inputStream = new BufferedReader(new InputStreamReader(new DataInputStream(socket.getInputStream())));
            m_response_stream = new BufferedInputStream((socket.getInputStream()));

        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMessage(String host_name, String query){
        System.out.println("Sending Message...");
//        System.out.println(url.getQuery());
        m_output_stream.println("GET " + host_name + " HTTP/1.1\r");
        m_output_stream.println("Host: " + query + "\r");
//        m_outputStream.println("Connection: Keep-Alive\r");
//        m_outputStream.println("Accept-Encoding: gzip, deflate\r");
        m_output_stream.println("\r");
        m_output_stream.flush();
    }
    
    private int readLineFromInputStream(byte[] response_buffer, int total_length){
        int index = 0;
        try{
            index = 0;
            while(true){
                int read_byte = m_response_stream.read();
                if((char)read_byte == '\n'){
                    index--; // Everything ends with \r\n
                    break;
                }else if(read_byte < 0){
                    break;
                }
                response_buffer[index++] = (byte)read_byte;
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
        System.out.println("Reading Header...");
        HashMap<String, String> response_header = new HashMap(); 
        
       
        byte[] response_buffer = new byte[1000];
//        int bytes_read = m_response_stream.read(response_buffer);
        int bytes_read = readLineFromInputStream(response_buffer, 1000);
        String message = new String(response_buffer, 0, bytes_read);
        System.out.println(message);

        response_header.put("Status", message);
        while(true){
            bytes_read = readLineFromInputStream(response_buffer, 1000);
            message = new String(response_buffer, 0, bytes_read);
            System.out.println(message.length() + "-'" + message +"'");
            if(message.length() == 0) break;
            String[] key_value_pair = message.split("[:]");
            response_header.put(key_value_pair[0].replaceAll("\\s+",""), key_value_pair[1].replaceAll("\\s+",""));
        }
        
        
        return response_header;
    }
    
    private ArrayList<byte[]> readResponseData(int content_length){
        System.out.println("Reading Data...");
        ArrayList<byte[]> total_data = new ArrayList();
        int total_bytes_read = 0;
        
        byte[] response_buffer = new byte[1000];
        int bytes_read;
        while(true){
            bytes_read = readLineFromInputStream(response_buffer, 1000);
            
            total_bytes_read += bytes_read;
            
//            String message = new String(response_buffer, 0, bytes_read);
//            System.out.println(total_bytes_read + "-'" + message + "'");
                
            byte[] data = Arrays.copyOf(response_buffer, bytes_read);
            total_data.add(data);
            
            if(total_bytes_read >= content_length) break;
        }
        
        
        return total_data;
    }
    
    private void closeConnection(){
        System.out.println("Closing Connection...");
        try{
            m_response_stream.close();
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_output_stream.close();
    }
    
    
    public static void main(String[] args){
        TCPClient tcpClient = new TCPClient();

//        tcpClient.makeRequest("people.ucalgary.ca");
//        tcpClient.makeRequest("people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
        tcpClient.makeRequest("www.google.ca");
//        tcpClient.makeRequest("www.tutorialspoint.com/http/http_requests.htm");
//        tcpClient.makeRequest("people.ucalgary.ca/~mghaderi/test/uc.gif");
    }
} 