package com.danga.MemCached.Flare.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.danga.MemCached.SockIOPool;
import com.danga.MemCached.Flare.FlaredClient;

public class FlaredClientUnitTests {

    // logger
    private static Logger log =
        Logger.getLogger( FlaredClientUnitTests.class.getName() );

    public static FlaredClient fc  = null;
    
    private static String[] serverlist;

    public static void test3() {
        String input = "test of string encoding";
        fc.set( "foo", input );
        String s = (String)fc.get( "foo" );
        assert s.equals( input );
        log.error( "+ store/retrieve String type test passed" );
    }
    
    public static void testDump() {
        fc.flushAll();
        
        Float f = new Float( 1.1f );
        fc.set( "key1", f );
        String input = "test of string encoding";
        fc.set( "key2", input );
        TestClass tc = new TestClass( "foo", "bar", new Integer( 32 ) );
        fc.set( "key3", tc );
        
        Map<String, Object> src = new HashMap<String, Object>();
        src.put( "key1", f );
        src.put( "key2", input );
        src.put( "key3", tc );
        
        Map<String, Map<String, Object>> map = fc.dump();
        assert map != null;
        assert map.size() == serverlist.length;

        for (String server : serverlist) {
            Map<String, Object> dump = map.get(server);
            assert dump != null;
            for (Map.Entry<String, Object> e : dump.entrySet()) {
                Object obj = src.get(e.getKey());
                assert obj != null;
                assert obj.equals(e.getValue());
            }
        }

        log.error( "+ dump test passed" );
    }

    public static void runAlTests( FlaredClient fc ) {
      for ( int t = 0; t < 2; t++ ) {
          fc.setCompressEnable( ( t&1 ) == 1 );
          test3();
      }
      testDump();
    }
    
    /**
     * This runs through some simple tests of the FlareClient.
     */
    public static void main(String[] args) {

        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel( Level.WARN );

        if ( !FlaredClientUnitTests.class.desiredAssertionStatus() ) {
            System.err.println( "WARNING: assertions are disabled!" );
            try { Thread.sleep( 3000 ); } catch ( InterruptedException e ) {}
        }
        
      serverlist = new String[] {
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

        fc = new FlaredClient( "test" );
        fc.setSync( true );
        runAlTests( fc );
    }

    /** 
     * Class for testing serializing of objects. 
     * 
     * @author $Author: $
     * @version $Revision: $ $Date: $
     */
    @SuppressWarnings("serial")
    public static final class TestClass implements Serializable {

        private String field1;
        private String field2;
        private Integer field3;

        public TestClass( String field1, String field2, Integer field3 ) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public String getField1() { return this.field1; }
        public String getField2() { return this.field2; }
        public Integer getField3() { return this.field3; }

        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( !( o instanceof TestClass ) ) return false;

            TestClass obj = (TestClass)o;

            return ( ( this.field1 == obj.getField1() || ( this.field1 != null && this.field1.equals( obj.getField1() ) ) )
                    && ( this.field2 == obj.getField2() || ( this.field2 != null && this.field2.equals( obj.getField2() ) ) )
                    && ( this.field3 == obj.getField3() || ( this.field3 != null && this.field3.equals( obj.getField3() ) ) ) );
        }
    }
}
