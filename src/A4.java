/*  A4.java - Driver code
 *
 *  @version CS 391 - Spring 2018 - A4
 *
 **/

import java.io.*;
import java.net.*;
import java.util.*;

public class A4 {

    /* Start a new peer whose parameters are given at the command line
     */
    public static void main(String args[]) {
	String peerName, peerIP, filesPath, neighborIP;
	int lPort, neighborPort;
	int numArgs = args.length;
	try {
	    if (numArgs !=4 && numArgs != 6) {
		System.err.println("Usage:");
		System.err.println("   java A4 <peer name> <peer IP> " +
				   "<peer lookup port> <path to files> " +
				   "[ <neighbor IP> <neighbor lookup port> ]");
		System.exit(1);
	    }
	    peerName = args[0];
	    peerIP = args[1];
	    lPort = Integer.parseInt(args[2]);
	    filesPath = args[3];
	    if (args.length == 6) {		
		neighborIP = convertNameToIP(args[4]);
		neighborPort = Integer.parseInt(args[5]);
	    } else {
		neighborIP = null;
		neighborPort = -1;
	    }
	    new Peer(peerName, peerIP, lPort, filesPath, neighborIP, 
		     neighborPort).run();
	} catch (NumberFormatException e) {
	    System.out.println("One of the arguments is a malformed number.");
	}
    }// main method

    /* Make a DNS call, if needed. 
       If the argument is a symbolic host name, return its IP address as a
       String representation of its dotted decimal address. Example:
                 return "127.0.0.1" given "localhost"
       If the argument is the string representation of an IP address in dotted
       decimal, just return it (no DNS call is made). Example:
                 return "127.0.0.1" given "127.0.0.1"
     */
    static String convertNameToIP(String hostName) {
	String result = null;
	try {
	    result = InetAddress.getByName(hostName).toString();
	    int slash = result.indexOf("/");
	    if (slash > -1 ) {
		result = result.substring(slash+1);
	    }
	} catch (UnknownHostException e) {
	    System.err.println("Unknown host: " + hostName);
	    System.exit(1);
	}
	return result;	
    }// convertNameToIP method
}// A4 class
