/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructure;


import java.util.ArrayList;

import java.util.HashMap;


/**
 *
 * @author cow
 */
public class Data {

    public URL m_current_URL;
    public HashMap<String, String> m_response_header;
    public ArrayList<byte[]> m_response_data;
//    public static final SimpleDateFormat M_Date_Time_Formater = new SimpleDateFormat("EEE, dd MMM yyyy HH:MM:SS");
    
    public Data(){
//        M_Date_Time_Formater.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    public void newURL(String url_name){
        m_current_URL = new URL(url_name);
    }
    
    public void setURL(URL url){
        m_current_URL = url;
    }
    
    public void setResponseHeader(HashMap<String, String> response_header){
        m_response_header = response_header;
    }
    
    public void setData(ArrayList<byte[]> data){
        m_response_data = data;
    }
    
    public ArrayList<byte[]> getData(){
        return m_response_data;
    }
    
    public int getContentLength(){
        if (m_response_header == null) return -1;
        String content_length = m_response_header.get("Content-Length");
        if (content_length == null) content_length = "0";
//        System.out.println("**" + content_length);
        return Integer.parseInt(content_length);
    }
    
    public String getHostName(){
        return m_current_URL.getHostName();
    }
    
    public int getPortNum(){
        return m_current_URL.getPortNum();
    }
    
    public String getQuery(){
        return m_current_URL.getQuery();
    }

    public HashMap<String, String> getHeader(){
        return m_response_header;
    }
    
    @Override
    public String toString(){
        boolean is_text  = false;
        String out = "";
        out += m_current_URL.m_host_name+":"+m_current_URL.getPortNum()+m_current_URL.m_query+"\n";
        
        for (String key : m_response_header.keySet()){
            out += key + ":" + m_response_header.get(key) + "\n";
            if(key.equals("Content-Type")){
                String[] content_type = m_response_header.get(key).split("[;]");
                
                if(content_type[0].equals("text/html") || content_type[0].equals("text/plain")){
                    is_text = true;
                }
            } 
        }
        out += "\n";
        int data_len = 0;
        data_len = m_response_data.stream().map((data_line) -> data_line.length).reduce(data_len, Integer::sum);//            if(is_text){

//        for(byte[] data_line : m_response_data){
////            if(is_text){
////                String line = new String(data_line, 0, data_line.length);
////                out += line + "\n";
////            }
//            data_len += data_line.length;
//        }
//        out += "\n";
        out += "Data Read Length: " + data_len;
        return out;
    }
    
    public static void main(String[] args){
        
//        System.out.println(Data.getServerTime());
//        System.out.println(Calendar.getInstance().getTime());
    }
}

