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
        return Integer.parseInt(m_response_header.get("Content-Length"));
    }
    
    public String getHostName(){
        return m_current_URL.getHostName();
    }
    
    public String getQuery(){
        return m_current_URL.getQuery();
    }
}

