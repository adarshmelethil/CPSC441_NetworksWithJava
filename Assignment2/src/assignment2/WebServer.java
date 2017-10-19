/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment2;


import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author cow
 */
public class WebServer extends Thread{
    // Server Port number
    int m_port;
    // Server Socket
    ServerSocket m_server_socket; 
    // Variable to control the server
    boolean m_server_active;
    
    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * @throws java.io.IOException
     * 
     */
    public WebServer(int port) {
	m_port = port;
        m_server_active = true;
    }
	
    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
     *   Remains in listening mode until shutdown signal
     * 
     */
    @Override
    public void run() {
        try {
            m_server_socket = new ServerSocket(m_port);
        } catch (IOException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        while(m_server_active){
            try { 
                Socket client_socket = m_server_socket.accept();
                
            } catch (IOException ex) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

	
    /**
     * Signals the server to shutdown.
     *
     */
    public void shutdown() {
        m_server_active = false;
    }

	
    /**
     * A simple driver.
     */
    public static void main(String[] args) {
        int serverPort = 2225;

        // parse command line args
        if (args.length == 1) {
                serverPort = Integer.parseInt(args[0]);
        }

        if (args.length >= 2) {
                System.out.println("wrong number of arguments");
                System.out.println("usage: WebServer <port>");
                System.exit(0);
        }

        System.out.println("starting the server on port " + serverPort);

        WebServer server = null;
        server = new WebServer(serverPort);

        server.start();
        System.out.println("server started. Type \"quit\" to stop");
        System.out.println(".....................................");

        Scanner keyboard = new Scanner(System.in);
        while ( !keyboard.next().equals("quit") );

        System.out.println();
        System.out.println("shutting down the server...");
        server.shutdown();
        System.out.println("server stopped");
    }
}
