package com.danga.MemCached.Flare;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.danga.MemCached.SockIOPool;

public class FlareiClient extends FlareClient {

    // logger
    private static Logger log =
        Logger.getLogger( FlareiClient.class.getName() );

    /**
     * Creates a new instance of FlareiClient.
     */
    public FlareiClient() {
    }

    /** 
     * Creates a new instance of FlareiClient
     * accepting a passed in pool name.
     * 
     * @param poolName name of SockIOPool
     */
    public FlareiClient( String poolName ) {
        super(poolName);
    }

    /** 
     * Creates a new instance of FlareiClient but
     * acceptes a passed in ClassLoader.
     * 
     * @param classLoader ClassLoader object.
     */
    public FlareiClient( ClassLoader classLoader ) {
        super(classLoader);
    }

    /** 
     * Creates a new instance of FlareiClient but
     * acceptes a passed in ClassLoader and a passed
     * in ErrorHandler.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     */
    public FlareiClient( ClassLoader classLoader, ErrorHandler errorHandler ) {
        super(classLoader, errorHandler);
    }

    /** 
     * Creates a new instance of FlareiClient but
     * acceptes a passed in ClassLoader, ErrorHandler,
     * and SockIOPool name.
     * 
     * @param classLoader ClassLoader object.
     * @param errorHandler ErrorHandler object.
     * @param poolName SockIOPool name
     */
    public FlareiClient( ClassLoader classLoader, ErrorHandler errorHandler, String poolName ) {
        super(classLoader, errorHandler, poolName);
    }
    
    
    public enum Role {
        MASTER("master"), SLAVE("slave"), PROXY("proxy");

        private String role;
        
        private Role(String role) {
            this.role = role;
        }
        
        public String toString() {
            return this.role;
        }
    }
    
    /**
     * shift node role
     * 
     * @param serverName
     * @param port
     * @param role
     * @param balance
     * @return success true/false
     */
    public boolean shiftNodeRole( String serverName, int port, Role role, int balance ) {
        return shiftNodeRole( serverName, port, role, balance, -1);
    }
    
    /**
     * shift node role
     * 
     * @param serverName
     * @param port
     * @param role
     * @param balance
     * @param partition
     * @return success true/false
     */
    public boolean shiftNodeRole( String serverName, int port, Role role, int balance, int partition ) {
        SockIOPool pool = getPool();
        String[] servers = pool.getServers();
        if (servers.length != 1) {
            log.error( "++++ the count of index server is 1." );
            return false;
        }
        
        SockIOPool.SockIO sock = pool.getConnection(servers[0]);
        if ( sock == null ) {
            log.error( "++++ unable to get connection to : " + servers[0] );
            if ( errorHandler != null )
                errorHandler.handleErrorOnNodeRole( this, new IOException( "no socket to server available" ) );
            return false;
        }
        
        boolean success = true;

        // build command
        String command = "node role " + serverName + " " + port + " " + role + " " + balance + " " + partition + "\r\n";

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
                errorHandler.handleErrorOnNodeRole( this, e );

            // exception thrown
            log.error( "++++ exception thrown while writing bytes to server on node role" );
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
    
    
    public enum State {
        ACTIVE("active"), PREPARE("prepare"), DOWN("down");

        private String state;
        
        private State(String state) {
            this.state = state;
        }
        
        public String toString() {
            return this.state;
        }
    }
    
    /**
     * shift node state
     * 
     * @param serverName
     * @param port
     * @param state
     * @return success true/false
     */
    public boolean shiftNodeState( String serverName, int port, State state ) {
        SockIOPool pool = getPool();
        String[] servers = pool.getServers();
        if (servers.length != 1) {
            log.error( "++++ the count of index server is 1." );
            return false;
        }
        
        SockIOPool.SockIO sock = pool.getConnection(servers[0]);
        if ( sock == null ) {
            log.error( "++++ unable to get connection to : " + servers[0] );
            if ( errorHandler != null )
                errorHandler.handleErrorOnNodeState( this, new IOException( "no socket to server available" ) );
            return false;
        }
        
        boolean success = true;

        // build command
        String command = "node state " + serverName + " " + port + " " + state + "\r\n";

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
                errorHandler.handleErrorOnNodeState( this, e );

            // exception thrown
            log.error( "++++ exception thrown while writing bytes to server on node state" );
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
    
    /**
     * remove node from index server (only available if target node is down).
     * 
     * @param serverName
     * @param port
     * @return success true/false
     */
    public boolean removeNode( String serverName, int port ) {
        SockIOPool pool = getPool();
        String[] servers = pool.getServers();
        if (servers.length != 1) {
            log.error( "++++ the count of index server is 1." );
            return false;
        }
        
        SockIOPool.SockIO sock = pool.getConnection(servers[0]);
        if ( sock == null ) {
            log.error( "++++ unable to get connection to : " + servers[0] );
            if ( errorHandler != null )
                errorHandler.handleErrorOnRemoveNode( this, new IOException( "no socket to server available" ) );
            return false;
        }
        
        boolean success = true;

        // build command
        String command = "node remove " + serverName + " " + port + "\r\n";

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
                errorHandler.handleErrorOnRemoveNode( this, e );

            // exception thrown
            log.error( "++++ exception thrown while writing bytes to server on node state" );
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
