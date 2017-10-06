/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructure;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cow
 */
public class Data {

    public URL m_current_URL;
    public HashMap<String, String> m_response_header;
    public ArrayList<byte[]> m_response_data;
    private static final SimpleDateFormat m_Date_Time_Formater = new SimpleDateFormat("EEE,ddMMMyyyy HH:mm:ss zzz");
    
    public void newURL(String url_name){
        m_current_URL = new URL(url_name);
    }
    
    public void setResponseHeader(HashMap<String, String> response_header){
        m_response_header = response_header;
        String lm = response_header.get("Last-Modified");
//        Date d = null;
//        try {
//            d = m_Date_Time_Formater.parse(lm);
//        } catch (ParseException ex) {
//            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println(d);
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
                
                if(content_type[0].equals("text/html") || content_type[0].equals("text/plain")){
                    is_text = true;
                }
            } 
        }
        out += "\n";
        int data_len = 0;
        for(byte[] data_line : m_response_data){
//            if(is_text){
//                String line = new String(data_line, 0, data_line.length);
//                out += line + "\n";
//            }
            data_len += data_line.length;
        }
//        out += "\n";
        out += "Data Read Length: " + data_len;
        return out;
    }
    
    public static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
    
    public static void main(String[] args){
        
        System.out.println(Data.getServerTime());
        System.out.println(Calendar.getInstance().getTime());
    }
}

