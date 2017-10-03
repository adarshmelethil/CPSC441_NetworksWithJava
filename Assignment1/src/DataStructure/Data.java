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

    public URL currentURL;
    public HashMap<String, String> response_header;
    public ArrayList<String> response_data;
    public void newURL(String url_name){
        currentURL = new URL(url_name);
    }
    
    public void setResponseHeader(HashMap rh){
        response_header = rh;
    }
    
    public void setData(ArrayList<String> data){
        response_data = data;
    }

//    public String getHost(){
//        return currentURL.getHostName();
//    }
//    
//    public String getQuery(){
//        return currentURL.getQuery();
//    }
}

