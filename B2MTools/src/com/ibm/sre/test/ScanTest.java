/**
 * 
 */
package com.ibm.sre.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.sre.tools.LogScan;

/**
 * @author DALE Nilsson
 *
 */
public class ScanTest {
    public static LogScan scanner;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	scanner = new LogScan();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link com.ibm.sre.tools.LogScan#main(java.lang.String[])}.
     */
    @Test
    public final void testMain() {
	
	fail("Not yet implemented");
    }

}
