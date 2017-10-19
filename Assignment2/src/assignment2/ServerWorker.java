/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author cow
 */
public class ServerWorker extends Thread{
    Socket m_socket;
    BufferedReader m_input_request;
    OutputStream m_output_response;
    /*
     * Default Constructor
     */
    ServerWorker(Socket socket) throws IOException{
        m_socket = socket;
        m_input_request = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        m_output_response = m_socket.getOutputStream();
    }
    
    private String processRequest(ArrayList<String> request){
        String response = "";
        for(String line: request){
            response += line;
        }
        return response;
    }
    /*
     * Thread run override
     */
    @Override
    public void run(){
        try {
            ArrayList<String> request = new ArrayList();
            String input_line;
            while((input_line = m_input_request.readLine()) != null){
                request.add(input_line);
            }
            String response = processRequest(request);
            System.out.println(response);
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
