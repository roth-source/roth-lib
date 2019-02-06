package roth.lib.java.jdbc;

import java.sql.Connection;

import com.mchange.v2.c3p0.ConnectionCustomizer;

public class JdbcConnectionCustomizer implements ConnectionCustomizer
{
    public void onAcquire( Connection c, String pdsIdt )
    { 
       System.out.println("Acquired " + c ); 
    }

    public void onDestroy( Connection c, String pdsIdt )
    { 
    	System.out.println("Destroying " + c );
    }

    public void onCheckOut( Connection c, String pdsIdt )
    { 
    	System.out.println("Checked out " + c ); 
    }

    public void onCheckIn( Connection c, String pdsIdt )
    { 
    	System.out.println("Checking in " + c ); 
    }
}
