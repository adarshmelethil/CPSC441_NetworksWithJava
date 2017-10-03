package assignment1;

import DataStructure.Data;
import DataStructure.URL;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient{
    
    // Response FROM server
    PrintWriter m_outputStream;
    // Request stream TO server
    BufferedReader m_inputStream;
    
    public void makeRequest(String url_name){
        Data data = new Data();
        data.newURL(url_name);
//        System.out.println(data.currentURL);
        
        openConnection(data.currentURL);
        sendMessage(data.currentURL);
        data.setResponseHeader(readResponseHeader());
        data.setData(readResponseData());
        closeConnection();
    }
    
    private void openConnection(URL url){
//        System.out.println("Opening Connection...");
        try {
            Socket socket;
            socket = new Socket(url.getHostName(), 80);
        
            m_outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
            m_inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMessage(URL url){
//        System.out.println("Sending Message...");
//        System.out.println(url.getQuery());
        m_outputStream.println("GET " + url.getQuery() + " HTTP/1.1\r");
        m_outputStream.println("Host: " + url.getHostName() + "\r");
//        m_outputStream.println("Connection: Keep-Alive\r");
//        m_outputStream.println("Accept-Encoding: gzip, deflate\r");
        m_outputStream.println("\r");
        m_outputStream.flush();
    }
    
    private HashMap<String, String> readResponseHeader(){
//        System.out.println("Reading Header...");
        HashMap<String, String> response_header = new HashMap(); 
        String line;
        try{
            line = m_inputStream.readLine();
            response_header.put("Status", line);
            while((line = m_inputStream.readLine()) != null){
    //            line = m_inputStream.nextLine();

//                System.out.println("Response: " + line);
                if (line.length() == 0) break;
                String[] key_value = line.split("[:]");
                if(key_value[0].equals("Content-Type")){
                    String[] k_v_2 = key_value[1].split("[;]");
                    if(k_v_2.length < 1){
                        String[] k_v_3 = k_v_2[1].split("[=]");
                        response_header.put(key_value[0].replaceAll("\\s+",""), k_v_2[0].replaceAll("\\s+",""));
                        response_header.put(k_v_3[0].replaceAll("\\s+",""), k_v_3[1].replaceAll("\\s+",""));
                        continue;
                    }
                }
                response_header.put(key_value[0].replaceAll("\\s+",""), key_value[1].replaceAll("\\s+",""));
            }
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return response_header;
    }
    
    private ArrayList<String> readResponseData(){
        ArrayList<String> data = new ArrayList();
        String line;
        try {
            while((line = m_inputStream.readLine()) != null){
                data.add(line);
//                System.out.println(line);
            }
        }catch(UnsupportedEncodingException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    private void closeConnection(){
//        System.out.println("Closing Connection...");
        try{
            m_inputStream.close();
        }catch(IOException ex){
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        m_outputStream.close();
    }
    
    
    public static void main(String[] args){
        TCPClient tcpClient = new TCPClient();

//        tcpClient.makeRequest("people.ucalgary.ca");
//        tcpClient.makeRequest("people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
//        tcpClient.makeRequest("www.google.ca");
//        tcpClient.makeRequest("www.tutorialspoint.com/http/http_requests.htm");
        tcpClient.makeRequest("people.ucalgary.ca/~mghaderi/test/uc.gif");
    }
} 