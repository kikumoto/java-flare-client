package com.danga.MemCached.Flare;

import org.apache.log4j.Logger;
import com.danga.MemCached.SockIOPool;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlaredClient extends FlareClient {

    // logger
    private static Logger log =
        Logger.getLogger( FlaredClient.class.getName() );

    /**
     * Creates a new instance of FlaredClient.
     */
    public FlaredClient() {
    }

    /** 
     * Creates a new instance of FlaredClient
     * accepting a passed in pool name.
     * 
     * @param poolName name of SockIOPool
     */
    public FlaredClient( String poolName ) {
        super(poolName);
    }

    /** 
     * Creates a new instance of FlaredClient but
     * acceptes a passed in ClassLoader.
     * 
     * @param classLoader ClassLoader object.
     */
    public FlaredClient( ClassLoader classLoader ) {
        super(classLoader);
    }

    /** 
     * Creates a new instance of FlaredClient but
     * acceptes a passed in ClassLoader and a passed
     * in ErrorHandler.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     */
    public FlaredClient( ClassLoader classLoader, ErrorHandler errorHandler ) {
        super(classLoader, errorHandler);
    }

    /** 
     * Creates a new instance of FlaredClient but
     * acceptes a passed in ClassLoader, ErrorHandler,
     * and SockIOPool name.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     * @param poolName SockIOPool name
     */
    public FlaredClient( ClassLoader classLoader, ErrorHandler errorHandler, String poolName ) {
        super(classLoader, errorHandler, poolName);
    }

    /**
     * Sets sync mode
     */
    public void setSync( boolean sync ) {
        this.sync = sync;
    }
    
    /**
     * dump data for all servers
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     * 
     * @return map
     */
    public Map<String, Map<String, Object>> dump() {
        return dump( null, 0, -1, 0, false);
    }
    
    /**
     * dump data for all servers
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     *
     * @param wait wait msec for each key retrieval.
     * @param partition target partition to dump.
     * @param partitionSize partition size to calculate partition 
     * @return map
     */
    public Map<String, Map<String, Object>> dump( int wait, int partition, int partitionSize ) {
        return dump( null, wait, partition, partitionSize, false);
    }
    
    /**
     * dump data for all servers
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     *
     * @param wait wait msec for each key retrieval.
     * @param partition target partition to dump.
     * @param partitionSize partition size to calculate partition 
     * @param asString if true, then type of value in the nested map is string.
     * @return map
     */
    public Map<String, Map<String, Object>> dump( int wait, int partition, int partitionSize, boolean asString ) {
        return dump( null, wait, partition, partitionSize, asString);
    }
    
    /**
     * dump data for passed in servers (or all servers).
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     *
     * @param servers string array of servers to dump, or all if this is null.
     * @return map
     */
    public Map<String, Map<String, Object>> dump( String[] servers ) {
        return dump( servers, 0, -1, 0, false);
    }
    
    /**
     * dump data for passed in servers (or all servers).
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     *
     * @param servers string array of servers to dump, or all if this is null.
     * @param wait wait msec for each key retrieval.
     * @param partition target partition to dump.
     * @param partitionSize partition size to calculate partition 
     * @return map
     */
    public Map<String, Map<String, Object>> dump( String[] servers, int wait, int partition, int partitionSize ) {
        return dump( servers, wait, partition, partitionSize, false);
    }
    
    /**
     * dump data for passed in servers (or all servers).
     * 
     * Returns a map keyed on the servername.
     * The value is another map which contains dump data for a server.
     *
     * @param servers string array of servers to dump, or all if this is null.
     * @param wait wait msec for each key retrieval.
     * @param partition target partition to dump.
     * @param partitionSize partition size to calculate partition 
     * @param asString if true, then type of value in the nested map is string.
     * @return map
     */
    public Map<String, Map<String, Object>> dump( String[] servers, int wait, int partition, int partitionSize, boolean asString ) {
        SockIOPool pool = getPool();
        
        // get all servers and iterate over them
        servers = (servers == null)? pool.getServers() : servers;

        // if no servers, then return early
        if ( servers == null || servers.length <= 0 ) {
            log.error( "++++ no servers to dump" );
            return null;
        }
        
        // dump result
        Map<String, Map<String, Object>> resultMap = 
            new HashMap<String, Map<String, Object>>();
        
        // build command
        String command = "dump " + wait + " " + partition + " " + partitionSize + "\r\n";

        for ( int i = 0; i < servers.length; i++ ) {
            SockIOPool.SockIO sock = pool.getConnection(servers[i]);
            if ( sock == null ) {
                log.error( "++++ unable to get connection to : " + servers[i] );
                if ( errorHandler != null )
                    errorHandler.handleErrorOnDump( this, new IOException( "no socket to server available" ) );
                continue;
            }
            
            try {
                sock.write( command.getBytes() );
                sock.flush();

                Map<String, Object> dump =
                    new HashMap<String, Object>();
                loadMulti(sock, dump, asString);
                resultMap.put( servers[i], dump );
                
            }
            catch ( IOException e ) {

                // if we have an errorHandler, use its hook
                if ( errorHandler != null )
                    errorHandler.handleErrorOnDump( this, e );

                // exception thrown
                log.error( "++++ exception thrown while writing bytes to server on dump" );
                log.error( e.getMessage(), e );

                try {
                    sock.trueClose();
                }
                catch ( IOException ioe ) {
                    log.error( "++++ failed to close socket : " + sock.toString() );
                }

                sock = null;
            }

            if ( sock != null ) {
                sock.close();
                sock = null;
            }
        }
        
        return resultMap;
    }
}
