package com.danga.MemCached.Flare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

abstract public class FlareClient extends MemCachedClient {

    // logger
    private static Logger log =
        Logger.getLogger( FlareClient.class.getName() );

    // optional error handler
    protected ErrorHandler errorHandler;
    
    
    /**
     * Creates a new instance of FlareClient.
     */
    public FlareClient() {
    }

    /** 
     * Creates a new instance of FlareClient
     * accepting a passed in pool name.
     * 
     * @param poolName name of SockIOPool
     */
    public FlareClient( String poolName ) {
        super(poolName);
    }

    /** 
     * Creates a new instance of FlareClient but
     * acceptes a passed in ClassLoader.
     * 
     * @param classLoader ClassLoader object.
     */
    public FlareClient( ClassLoader classLoader ) {
        super(classLoader);
    }

    /** 
     * Creates a new instance of FlareClient but
     * acceptes a passed in ClassLoader and a passed
     * in ErrorHandler.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     */
    public FlareClient( ClassLoader classLoader, ErrorHandler errorHandler ) {
        super(classLoader, errorHandler);
        this.errorHandler = errorHandler;
    }

    /** 
     * Creates a new instance of FlareClient but
     * acceptes a passed in ClassLoader, ErrorHandler,
     * and SockIOPool name.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     * @param poolName SockIOPool name
     */
    public FlareClient( ClassLoader classLoader, ErrorHandler errorHandler, String poolName ) {
        super(classLoader, errorHandler, poolName);
        this.errorHandler = errorHandler;
    }
    
    /**
     * ping for all servers
     * *
     * Returns a map keyed on the servername.
     * The value is ping result.
     * 
     * @return map
     */
    public Map<String, Boolean> ping() {
        return ping( null );
    }
    
    
    /**
     * ping for passed in servers (or all servers).
     * *
     * Returns a map keyed on the servername.
     * The value is ping result.
     * 
     * @param servers string array of servers to ping, or all if this is null
     * @return map
     */
    public Map<String, Boolean> ping( String[] servers ) {
        SockIOPool pool = getPool();
        
        // get all servers and iterate over them
        servers = (servers == null)? pool.getServers() : servers;

        // if no servers, then return early
        if ( servers == null || servers.length <= 0 ) {
            log.error( "++++ no servers to ping" );
            return null;
        }
        
        // array of ping result Map
        Map<String, Boolean> pingMap = 
            new HashMap<String, Boolean>();
        
        // build command
        String command = "ping\r\n";

        for ( int i = 0; i < servers.length; i++ ) {
            SockIOPool.SockIO sock = pool.getConnection(servers[i]);
            if ( sock == null ) {
                log.error( "++++ unable to get connection to : " + servers[i] );
                if ( errorHandler != null )
                    errorHandler.handleErrorOnPing( this, new IOException( "no socket to server available" ) );
                continue;
            }
            
            try {
                sock.write( command.getBytes() );
                sock.flush();

                String line = sock.readLine();
                pingMap.put(servers[i], OK.equals( line ));
            }
            catch ( IOException e ) {

                // if we have an errorHandler, use its hook
                if ( errorHandler != null )
                    errorHandler.handleErrorOnPing( this, e );

                // exception thrown
                log.error( "++++ exception thrown while writing bytes to server on ping" );
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
        
        return pingMap;
    }

    /** 
     * Retrieves stats nodes for all servers.
     *
     * Returns a map keyed on the servername.
     * The value is another map which contains node stats
     * with servername:port:field as key and value as value.
     * 
     * @return Stats map
     */
    public Map<String, Map<String, String>> statsNodes() {
        return statsNodes( null );
    }

    /** 
     * Retrieves stats for passed in servers (or all servers).
     *
     * Returns a map keyed on the servername.
     * The value is another map which contains node stats
     * with servername:port:field as key and value as value.
     * 
     * @param servers string array of servers to retrieve stats from, or all if this is null
     * @return Stats map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, String>> statsNodes( String[] servers ) {
        return stats( servers, "stats nodes\r\n", STATS );
    }

    /** 
     * Retrieves stats threads for all servers.
     *
     * Returns a map keyed on the servername.
     * The value is another map which contains thread stats
     * with [thread id]:field as key and value as value.
     * 
     * @return Stats map
     */
    public Map<String, Map<String, String>> statsThreads() {
        return statsThreads( null );
    }

    /** 
     * Retrieves stats for passed in servers (or all servers).
     *
     * Returns a map keyed on the servername.
     * The value is another map which contains thread stats
     * with [thread id]:field as key and value as value.
     * 
     * @param servers string array of servers to retrieve stats from, or all if this is null
     * @return Stats map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, String>> statsThreads( String[] servers ) {
        return stats( servers, "stats threads\r\n", STATS );
    }
    
    /**
     * kill specified thread in a server.
     * 
     * @param server Servername
     * @param tid Thread id identified via @link FlareClient#statsThreads()}
     * @return success true/false
     */
    public boolean kill( String server, long tid ) {
        // if no server, then return early
        if ( server == null) {
            log.error( "++++ no server to kill tid" );
            return false;
        }
        
        SockIOPool pool = getPool();
        SockIOPool.SockIO sock = pool.getConnection(server);
        if ( sock == null ) {
            log.error( "++++ unable to get connection to : " + server );
            if ( errorHandler != null )
                errorHandler.handleErrorOnKill( this, new IOException( "no socket to server available" ) );
            return false;
        }
        
        boolean success = true;

        // build command
        String command = "kill " + tid + "\r\n";

        try {
            sock.write( command.getBytes() );
            sock.flush();

            // if we get appropriate response back, then we return true
            String line = sock.readLine();
            success = OK.equals( line );
        }
        catch ( IOException e ) {

            // if we have an errorHandler, use its hook
            if ( errorHandler != null )
                errorHandler.handleErrorOnPing( this, e );

            // exception thrown
            log.error( "++++ exception thrown while writing bytes to server on ping" );
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
        
        return success;
    }

}
