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
    
    public void newURL(String url_name){
        m_current_URL = new URL(url_name);
    }
    
    public void setResponseHeader(HashMap rh){
        m_response_header = rh;
    }
    
    public void setData(ArrayList<byte[]> data){
        m_response_data = data;
    }
    
    public int getContentLength(){
        if (m_response_header == null) return -1;
        String content_length = m_response_header.get("Content-Length");
        if (content_length == null) content_length = "0";
        return Integer.parseInt(content_length);
    }
    
    public String getHostName(){
        return m_current_URL.getHostName();
    }
    
    public String getQuery(){
        return m_current_URL.getQuery();
    }

    
    @Override
    public String toString(){
        boolean is_text  = false;
        String out = "";
        for (String key : m_response_header.keySet()){
            out += key + ":" + m_response_header.get(key) + "\n";
            if(key.equals("Content-Type")){
                String[] content_type = m_response_header.get(key).split("[;]");
                if(content_type[0].equals("text/html")){
                    is_text = true;
                }
            }
        }
        out += "\n";
        int data_len = 0;
        for(byte[] data_line : m_response_data){
            if(is_text){
                String line = new String(data_line, 0, data_line.length);
                out += line + "\n";
            }
            data_len += data_line.length;
        }
        out += "\n";
        out += "Data Read Length: " + data_len;
        return out;
    }
}

