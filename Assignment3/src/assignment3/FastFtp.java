
/**
 * FastFtp Class
 *
 */

import cpsc441.a3.shared.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FastFtp {
    private final int UDP_PORT = 2226;
    private final int THREAD_SLEEP = 10;
    Socket m_socket;
    DataInputStream m_input_stream;
    DataOutputStream m_output_stream;
    
    int m_server_UDP_PORT;
    String m_serverName;

    DatagramSocket m_datagram_socket;
    
    TxQueue m_queue;
    int m_rtoTimer;
    ScheduledExecutorService m_scheduler;
    ScheduledFuture<?> m_time_thread_Handle = null;
    
    byte[] m_data;
    
    int m_final_seq;
    
    class AckThread extends Thread{      
        @Override
        public void run(){
            System.out.println("AckThread!");
            try {
                // LOOP TO RECEIVE ACK
                while(true){
                    if(m_queue.isEmpty()){
                        Thread.sleep(THREAD_SLEEP);
                    }
                    
                    byte[] receive_buffer = new byte[4];
                    DatagramPacket receivePacket = new DatagramPacket(receive_buffer, receive_buffer.length);
                    m_datagram_socket.receive(receivePacket);
                    // String modifiedSentence2 = new String(sendPacket.getData());
                    ByteBuffer byte_buffer = ByteBuffer.wrap(receivePacket.getData());
                    byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
                    int recieved_int = byte_buffer.getInt();
                    
                    System.out.println(" FROM SERVER:" + recieved_int);
                    
                    while(true){
                        if(m_queue.isEmpty()){
                            break;
                        }
                        if( m_queue.element().getSeqNum() >= recieved_int){
                            break;
                        }
                        m_queue.remove();
                    }
                    if(recieved_int == m_final_seq){
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Ack Thread stopped receiving!");
//                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
            
            } catch (InterruptedException ex) {
                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex){
                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, "Null pointer in Ack Thread", ex);
            } finally{
                if (m_time_thread_Handle != null){
                    m_time_thread_Handle.cancel(true);
                    System.out.println("Stopped time thread!");
                }
                if ( m_scheduler != null ){
                    m_scheduler.shutdown();
                }
                if (m_datagram_socket != null){
                    m_datagram_socket.close();
                    System.out.println("Socket closed!");
                }
                System.out.println("Finished Ack Thread!");
            }
         

        }
        
    } 
    
    class TimeOutThread implements Runnable {
        int cur_seq = -1;
       
        @Override
        public void run() {
            // System.out.println("Checking Time!");
            if(m_queue.isEmpty()){
                return;
            }
            if (cur_seq < 0){
                cur_seq = m_queue.element().getSeqNum();
                return;
            }
                        
            int queue_seq = m_queue.element().getSeqNum();
            if( cur_seq == queue_seq ){
                System.out.print("TIME OUT! Resent:");
                Segment[] segments_in_queue = m_queue.toArray();

//                System.out.println("Resent: ");
//                while(!m_queue.isEmpty()){
//                    try {
//                        Segment seg = m_queue.remove();
//                        
//                        System.out.println( seg.getSeqNum() + "-" + new String(seg.getBytes()) );
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
                
                for(int i = 0; i < segments_in_queue.length; i++){
                    try {
                        byte[] send_data = segments_in_queue[i].getBytes();
                        System.out.print(segments_in_queue[i].getSeqNum() + " ");
                        DatagramPacket sendPacket = new DatagramPacket(send_data, send_data.length, InetAddress.getByName(m_serverName), m_server_UDP_PORT);
                        m_datagram_socket.send(sendPacket);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("");
                
                return;
            }else{
                cur_seq = queue_seq;
                return;
            }
            
        }
    }
    
    /**
     * Constructor to initialize the program 
     * 
     * @param windowSize	Size of the window for Go-Back_N in terms of segments
     * @param rtoTimer		The time-out interval for the retransmission timer
     */
    public FastFtp(int windowSize, int rtoTimer) {
        System.out.println("FastFtp Created!");
        System.out.println("Window size: "+ windowSize);
        System.out.println("Retransmission time: " + rtoTimer);

        m_queue = new TxQueue(windowSize);
        m_rtoTimer = rtoTimer;
    }
    
    public byte[] readFile(String fileName){
        System.out.print("Reading file: ");
        File file = new File(fileName);
        System.out.println(file.getAbsolutePath());
        byte[] file_bytes = null;
        
        FileInputStream file_input = null;
        try{
            file_input = new FileInputStream(file);
            
            file_bytes = new byte[(int) file.length()];
            file_input.read(file_bytes);
            
            
//            String file_content = new String(file_bytes);
//            System.out.println("File Content: \n" + file_content);
            
        
        } catch (FileNotFoundException ex){
            Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, "File Not Found!", ex);
        } catch (IOException ex) {
            Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, "Error reading file", ex);
        } finally {
            try {
                if (file_input != null){
                    file_input.close();
                }
            }catch (IOException ex){
                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, "Error closing file", ex);
            }
        }
        
        System.out.println("File size read: " + file_bytes.length);
        return file_bytes;
    }
    
    private boolean handshake(String server_name, int server_port, String fileName){
        try {
            // to be completed
            m_socket = new Socket(server_name, server_port);
            m_input_stream = new DataInputStream(m_socket.getInputStream());
            m_output_stream = new DataOutputStream(m_socket.getOutputStream());
            
            m_data = readFile(fileName);
            if (m_data == null){
                System.out.println("Failed to read file!");
                return false;
            }
            
            // Hand shake start {
            m_output_stream.writeUTF(fileName);
            m_output_stream.writeLong(m_data.length);
            m_output_stream.writeInt(UDP_PORT);
            m_output_stream.flush();
            
            int wait_time = 0;
            while(true){
                if(m_input_stream.available()>0){
                    System.out.println("Response at: "  + wait_time);
                    break;
                }
                
                try {
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                wait_time += 1;
                if (wait_time >= m_rtoTimer) {
                    System.out.println("Wait time exceeded for handshake!");
                    return false;
                }
            }
            
            m_server_UDP_PORT = m_input_stream.readInt();
            System.out.println("Server UDP port: " + m_server_UDP_PORT);
        } catch (IOException ex) {
            Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    private ArrayList<Segment> makeListOfSegments(){

        ArrayList<Segment> segment_list = new ArrayList();
        int index_start = 0;
        int index_end = Math.min(m_data.length, index_start + Segment.MAX_PAYLOAD_SIZE) ;
        
        
        while(true){
//            System.out.println(index_start + " - " + index_end);
            segment_list.add(new Segment(Arrays.copyOfRange(m_data, index_start, index_end)));
            
            if(index_end >= m_data.length){
                break;
            }
            index_start = index_end;
            index_end = Math.min(m_data.length, index_start + Segment.MAX_PAYLOAD_SIZE) ;
            
        }
        System.out.println("Number Of segments: " + segment_list.size());
        return segment_list;
    }
    
    /**
     * Sends the specified file to the specified destination host:
     * 1. send file/connection infor over TCP
     * 2. start receving thread to process coming ACKs
     * 3. send file segment by segment
     * 4. wait until transmit queue is empty, i.e., all segments are ACKed
     * 5. clean up (cancel timer, interrupt receving thread, close sockets/files)
     * 
     * @param serverName	Name of the remote server
     * @param serverPort	Port number of the remote server
     * @param fileName		Name of the file to be trasferred to the rmeote server
     */
    public void send(String serverName, int serverPort, String fileName) {
        System.out.println("Sending!");
        System.out.println("Server name: " + serverName);
        System.out.println("Server port: " + serverPort);
        System.out.println("File name: " + fileName);
        m_serverName = serverName;
        m_datagram_socket = null;
        
        if(handshake(serverName, serverPort, fileName)){
            
            AckThread ack_thread = null;
            try {
                System.out.println("Successful  handshake!");
                ArrayList<Segment> segment_list = makeListOfSegments();
                m_final_seq = segment_list.size();
                int index_to_send = 0;
                int sequence_number = 0;
                m_datagram_socket = new DatagramSocket(UDP_PORT);

                ack_thread = new AckThread();
                ack_thread.start();
                
                m_scheduler = Executors.newScheduledThreadPool(1);
                TimeOutThread tot = new TimeOutThread();
                m_time_thread_Handle = m_scheduler.scheduleAtFixedRate(tot, 1, m_rtoTimer, MILLISECONDS);
                
                // MAIN LOOP TO SEND DATA
                while(index_to_send < segment_list.size()){
                    Segment current_segment = segment_list.get(index_to_send);
                    current_segment.setSeqNum(sequence_number);
                    
                    byte[] send_data = current_segment.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(send_data, send_data.length, InetAddress.getByName(m_serverName), m_server_UDP_PORT);
                    m_datagram_socket.send(sendPacket);
//                    String modifiedSentence1 = new String(sendPacket.getData());
                    System.out.println(" Sent data, seq: " + current_segment.getSeqNum());

                    m_queue.add(current_segment);
                    
//                    Segment[] seg = m_queue.toArray();
//                    for(int i = 0; i < seg.length; i++){
//                        System.out.print(seg[i].getSeqNum() + " ");
//                    }
//                    System.out.println("");
                    
//                    System.out.println("Size: " + m_queue.size());
                    while(m_queue.isFull()){
                        Thread.sleep(THREAD_SLEEP);
                        // System.out.println("Full Queue!");
                    }

                    sequence_number++;
                    index_to_send++;
                    
                }
  
            } catch (SocketException ex) {                
                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ack_thread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Finished Main Sending!");
            }
        }else{
            System.out.println("Unsuccessful handshake!");
            Logger.getLogger(FastFtp.class.getName()).log(Level.SEVERE, null, "Could not connect to server");
        }

    }
    
    public static void main(String[] args){
        int window_size = 4;
        int rto_time = 1000;
        
        boolean test_ftp = true;
        boolean test_readFile = false;
        
        if (test_readFile){
           FastFtp ftp = new FastFtp(window_size, rto_time);
           ftp.readFile("test.txt");
        }
        
        if (test_ftp){
            FastFtp ftp = new FastFtp(window_size, rto_time);
            ftp.send("localhost", 2225, "test.txt");
        }
        
        
    }
}
