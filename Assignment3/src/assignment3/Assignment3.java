/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


import cpsc441.a3.shared.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author cow
 */
public class Assignment3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        // Testing
        Socket socket;
        try {
            socket = new Socket("localhost", 2225);
            DataInputStream data_input_stream = new DataInputStream(socket.getInputStream());
            DataOutputStream data_output_stream = new DataOutputStream(socket.getOutputStream());
            
            Path path = Paths.get("test.txt");
            byte[] data = Files.readAllBytes(path);
            
            // Hand shake start {
            data_output_stream.writeUTF("test.txt");
            data_output_stream.writeLong(data.length);
            data_output_stream.writeInt(2225);
            data_output_stream.flush();
            
            
            if (data_input_stream.available()>0) {
                int server_response = data_input_stream.readInt();
                System.out.println("Server response: " + server_response);
            }
            // Hand shake end }
        } catch (IOException ex) {
            Logger.getLogger(Assignment3.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
}
