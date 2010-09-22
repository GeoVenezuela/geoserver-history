package org.geoserver.monitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.SoftValueHashMap;
import org.geotools.util.logging.Logging;

public class ReverseDNSPostProcessor implements Runnable {
    static final Logger LOGGER = Logging.getLogger(ReverseDNSPostProcessor.class);
    
    static Map<String, String> reverseLookupCache = new SoftValueHashMap<String, String>(100);

    RequestData data;
    
    public ReverseDNSPostProcessor(RequestData data) {
        this.data = data;
    }
    
    public void run() {
        String host = reverseLookupCache.get(data.getRemoteAddr());
        if(host == null) {
            try {
                InetAddress addr = InetAddress.getByName(data.getRemoteAddr());
                host = addr.getHostName();
            } catch(UnknownHostException e) {
                LOGGER.log(Level.FINE, "Error reverse looking up " + data.getRemoteAddr(), e);
            }
        }
        
        data.setRemoteHost(host);
    }

}