/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructure;

import java.util.HashMap;

/**
 *
 * @author cow
 */
public class URL{
    String m_host_name;
    String m_query;
    
    
    
    public URL(String url){
        parseURL(url);
    }

    private void parseURL(String url_name){
        int path_split_index = url_name.indexOf("/");
        if (path_split_index < 0){
            m_host_name = url_name;
            m_query = "/";
        }else{
            m_host_name = url_name.substring(0,path_split_index);
            m_query = url_name.substring(path_split_index);
        }
    }

    public String getHostName(){
        return m_host_name;
    }

    public String getQuery(){
        return m_query;
    }

    @Override
    public String toString(){
        String url_string = "Host: '" + m_host_name + "'\n";
        url_string += "Query: '" + m_query +"'\n";

        return url_string;
    }
}