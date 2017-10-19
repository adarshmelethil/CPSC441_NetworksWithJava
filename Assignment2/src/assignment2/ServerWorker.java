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
        
        System.out.println("Worker: Created");
    }
    
    private String processRequest(ArrayList<String> request){
        System.out.println("Worker: Processing...");
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
        System.out.println("Worker: Started running");
        
        try {
            m_input_request = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));

            ArrayList<String> request = new ArrayList();
            String input_line;
            System.out.println("Worker: Trying to read");
            input_line = m_input_request.readLine();
            request.add(input_line);
//            System.out.println("Worker");
//            while((input_line = m_input_request.readLine()) != null){
//                System.out.println("Worker: input: " + input_line);
//                request.add(input_line);
//            }
            String response = processRequest(request);
            System.out.println("Worker: output: " + response);
            
            m_output_response = m_socket.getOutputStream();
            
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Worker: Finished running");
    }
}
