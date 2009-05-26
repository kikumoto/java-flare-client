package com.danga.MemCached.Flare.test;

import java.util.Map;
import com.danga.MemCached.SockIOPool;
import com.danga.MemCached.Flare.FlareClient;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class FlareClientUnitTests {
	
	// logger
	private static Logger log =
		Logger.getLogger( FlareClientUnitTests.class.getName() );

    public static FlareClient fc  = null;

    public static void testPing() {
        Map<String, Boolean> map = fc.ping();
		assert map != null;
		for (Map.Entry<String, Boolean> e : map.entrySet()) {
            assert e.getValue();
        }
		
		log.error( "+ ping test passed" );
    }
    
    public static void testStatsNodes() {
        Map<String, Map<String, String>> stats = fc.statsNodes();
        assert stats != null;
        log.error( "+ stats nodes test passed" );

        for (Map.Entry<String, Map<String, String>> e : stats.entrySet()) {
            String serverName = e.getKey();
            for (Map.Entry<String, String> statEntry : e.getValue().entrySet()) {
                System.out.println(serverName + " " + statEntry.getKey() + " " + statEntry.getValue());
            }
        }
    }
    
    public static void testStatsThreads() {
        Map<String, Map<String, String>> stats = fc.statsThreads();
        assert stats != null;
        log.error( "+ stats threads test passed" );

        for (Map.Entry<String, Map<String, String>> e : stats.entrySet()) {
            String serverName = e.getKey();
            for (Map.Entry<String, String> statEntry : e.getValue().entrySet()) {
                System.out.println(serverName + " " + statEntry.getKey() + " " + statEntry.getValue());
            }
        }
    }
    
    public static void testKill() {
        assert fc.kill("127.0.0.1:12121", 2);
        log.error( "+ kill test passed" );
    }


	public static void runAlTests( FlareClient fc ) {
	    testPing();
	    testStatsNodes();
        testStatsThreads();
//        testKill();
//		for ( int t = 0; t < 2; t++ ) {
//			fc.setCompressEnable( ( t&1 ) == 1 );
//			
//		}

	}

	/**
	 * This runs through some simple tests of the FlareClient.
	 */
	public static void main(String[] args) {

		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel( Level.WARN );

		if ( !FlareClientUnitTests.class.desiredAssertionStatus() ) {
			System.err.println( "WARNING: assertions are disabled!" );
			try { Thread.sleep( 3000 ); } catch ( InterruptedException e ) {}
		}
		
      String[] serverlist = {
              "127.0.0.1:12121"
      };
      Integer[] weights = { 1 };
      
		if ( args.length > 0 )
			serverlist = args;

		// initialize the pool for memcache servers
		SockIOPool pool = SockIOPool.getInstance( "test" );
		pool.setServers( serverlist );
		pool.setWeights( weights );
		pool.setMaxConn( 250 );
		pool.setNagle( false );
		pool.setHashingAlg( SockIOPool.CONSISTENT_HASH );
		pool.initialize();

        fc = new FlareClientMock( "test" );
		runAlTests( fc );
	}
	
	private static class FlareClientMock extends FlareClient {
	    public FlareClientMock(String poolName) {
	        super(poolName);
	    }
	}
}
