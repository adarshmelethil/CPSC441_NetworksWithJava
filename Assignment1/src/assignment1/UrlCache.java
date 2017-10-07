package assignment1;
/**
 * UrlCache Class
 * 
 *
 */

import DataBasePackage.Database;
import DataStructure.Data;
import DataStructure.URL;
import NetworkConnection.TCPClient;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UrlCache {
    
    TCPClient m_tcp;
    Database m_db;
    Data m_current_data;
    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw runtime exception.
	 *
     * @throws IOException if encounters any errors/exceptions
     */
	public UrlCache() throws IOException {
            m_tcp = new TCPClient();
            m_db = new Database();
            m_current_data = null;
	}
	
    /**
     * Downloads the object specified by the parameter url if the local copy is out of date.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     * @throws IOException if encounters any errors/exceptions
     */
	public void getObject(String url) throws IOException {
            URL url_obj = new URL(url);
            HashMap<String,String> header = m_db.checkDatabase(url_obj);
            if (header == null){
                m_current_data = m_tcp.makeRequest(url_obj);
//                System.out.println("From server:\n" + m_current_data);
                m_db.insert(m_current_data);
            }else{
                m_current_data = new Data();
                m_current_data.setURL(url_obj);
                m_current_data.setResponseHeader(header);
                m_current_data.setData(m_db.loadDataFromFile(header.get("Data-Path")));
//                System.out.println("From db:\n" + m_current_data);
                
//                String date = m_current_data.getHeader().get("Last-Modified");
//                Data data = m_tcp.makeConditionalRequest(url_obj, date);
//                System.out.println("***\n" + data);
//                String d = m_current_data.getHeader().get("Last-Modified");
//                Date date = new Date(d);
//                System.out.println("-" + date);
//                System.out.println(d);
            }
            
        }
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     */
	public long getLastModified(String url) {
            long millis = 0;
            if(m_current_data != null){
                millis = new Date(m_current_data.getHeader().get("Last-Modified")).getTime();
            }
            return millis;
	}
        
    public static void main(String[] args){
        UrlCache uc = null;
        try {
            uc = new UrlCache();
//            uc.getObject("people.ucalgary.ca/~smithmr/2017webs/encm511_17/17_Labs/17_Familiarization_Lab/MockLEDInterface.cpp");
        
            uc.getObject("people.ucalgary.ca/~mghaderi/test/uc.gif");
        } catch (IOException ex) {
            Logger.getLogger(UrlCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
