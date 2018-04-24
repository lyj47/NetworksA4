/** Peer.java - A peer-to-peer file sharing program
 *
 *  @version CS 391 - Spring 2018 - A4
 *
 *  @author FIRST STUDENT'S FULL NAME GOES HERE
 *
 *  @author 2nd STUDENT'S FULL NAME GOES HERE (DELETE THIS LINE IF NOT NEEDED)
 * 
 *  @bug The initial join of a new peer [does/does not] work fully. [pick one]
 *
 *  @bug The Status command [does/does not] work fully. [pick one]
 *
 *  @bug The Find command [does/does not] work fully. [pick one]
 *
 *  @bug The Get command [does/does not] work fully. [pick one]
 * 
 *  @bug The Quit command [does/does not] work fully. [pick one]
 * 
 **/

import java.io.*;
import java.net.*;
import java.util.*;

class Peer {
    String name,    // symbolic name of the peer, e.g., "P1" or "P2"
	ip,         // IP address in dotted decimal notation, e.g., "127.0.0.1"
	filesPath;  // path to local file repository, e.g., "dir1/dir2/dir3"
    int lPort,      // lookup port number (permanent UDP port)
	ftPort;     // file transfer port number (permanent TCP port)
    List<Neighbor> neighbors;      // current neighbor peers of this peer
    LookupThread  lThread;         // thread listening to the lookup socket
    FileTransferThread  ftThread;  // thread list. to the file transfer socket
    int seqNumber;                 // identifier for next Find request (used to
                                   // control the flooding by avoiding loops)
    Scanner scanner;               // used for keyboard input
    HashSet<String> findRequests;  // record of all lookup requests seen so far

    /* Instantiate a new peer (including setting a value of 1 for its initial
       sequence number), launch its lookup and file transfer threads,
       (and start the GUI thread, which is already implemented for you)
     */
    Peer(String name2, String ip, int lPort, String filesPath, 
	 String nIP, int nPort) {

	/* to be completed */

    }// constructor

    /* display the commands available to the user
       Do NOT modify this method.
     */
    static void displayMenu() {
	System.out.println("\nYour options:");
	System.out.println("    1. [S]tatus");
	System.out.println("    2. [F]ind <filename>");
	System.out.println("    3. [G]et <filename> <peer IP> <peer port>");
	System.out.println("    4. [Q]uit");
	System.out.print("Your choice: ");
    }// displayMenu method

    /* input the next command chosen by the user
     */
    int getChoice() {

	/* to be completed */

	return -1;
    }// getChoice method
        
    /* this is the implementation of the peer's main thread, which
       continuously displays the available commands, input the user's
       choice, and executes the selected command, until the latter
       is "Quit"
     */
    void run() {

	/* to be completed */

    }// run method

    /* execute the Quit command, that is, send a "leave" message to all of the
       peer's neighbors, then terminate the lookup thread
     */
    void processQuitRequest() {

	/* to be completed */

    }// processQuitRequest method

    /* execute the Status command, that is, read and display the list
       of files currently stored in the local directory of shared
       files, then print the list of neighbors. The EXACT format of
       the output of this method (including indentation, line
       separators, etc.) is specified via examples in the assignment
       handout.
     */
    void processStatusRequest() {

	/* to be completed */

    }// processStatusRequest method

    /* execute the Find command, that is, prompt the user for the file
       name, then look it up in the local directory of shared
       files. If it is there, inform the user. Otherwise, send a
       lookup message to all of the peer's neighbors. The EXACT format
       of the output of this method (including the prompt and
       notification), as well as the format of the 'lookup' messages
       are specified via examples in the assignment handout. Do not forget
       to handle the Find-request ID properly.
     */
    void processFindRequest() {

	/* to be completed */

    }// processFindRequest method

    /* execute the Get command, that is, prompt the user for the file
       name and address and port number of the selected peer with the
       needed file. Send a "get" message to that peer and wait for its
       response. If the file is not available at that peer, inform the
       user. Otherwise, extract the file contents from the response,
       output the contents on the user's terminal and save this file
       (under its original name) in the local directory of shared
       files.  The EXACT format of this method's output (including the
       prompt and notification), as well as the format of the "get"
       messages are specified via examples in the assignment
       handout.
     */
    void processGetRequest() {

	/* to be completed */

    }// processGetRequest method

    /* create a text file in the local directory of shared files whose
       name and contents are given as arguments.
     */
    void writeFile(String fileName, String contents) {

	/* to be completed */

    }// writeFile method

    /* Send to the user's terminal the list of the peer's
       neighbors. The EXACT format of this method's output is specified by
       example in the assignment handout.
     */
    void printNeighbors() {	

	/* to be completed */

    }// printNeighbors method

    /* Do NOT modify this class
     */
    class Neighbor {
	String ip;
	int port;

	Neighbor(String ip, int port) {
	    this.ip = ip;
	    this.port = port;
	}// constructor

	public boolean equals(Object o) {
 	    if (o == this) { return true;  }
	    if (!(o instanceof Neighbor)) { return false;  }        
	    Neighbor n = (Neighbor) o;         
	    return n.ip.equals(ip) && n.port == port;
	}// equals method

	public String toString() {
	    return ip + ":" + port;
	}// toString method
    }// Neighbor class

    class LookupThread extends Thread {
	
	DatagramSocket socket = null;           // UDP server socket
	private volatile boolean stop = false;  // flag used to stop the thread

	/* Stop the lookup thread by closing its server socket. This
	   works (in a not-so-pretty way) because this thread's run method is
	   constantly listening on that socket.
	   Do NOT modify this method.
	 */
	public void terminate() {
	    stop = true;
	    socket.close();
	}// terminate method


	/* This is the implementation of the thread that listens on
	   the UDP lookup socket. First (at startup), if the peer has
	   exactly one neighbor, send a "join" message to this
	   neighbor. Otherwise, skip this step. Second, continuously
	   wait for an incoming datagram (i.e., a request), display
	   its contents in the GUI's Lookup panel, and process the
	   request using the helper method below.
	*/
	public void run() {

	    /* to be completed */

	}// run method

	/* This helper method processes the given request, which was
	   received by the Lookup socket. Based on the first field of
	   the request (i.e., the "join", "leave", "lookup", or "file"
	   keyword), perform the appropriate action. All actions are
	   quite short, except for the "lookup" request, which has its
	   own helper method below.
	 */
	void process(String request) {

	    /* to be completed */

	}// process method

	/* This helper method processes a "lookup" request received
	   by the Lookup socket. This request is represented by the
	   given tokenizer, which contains the whole request line,
	   minus the "lookup" keyword (which was removed from the
	   beginning of the request) by the caller of this method.
	   Here is the algorithm to process such requests:
           If the peer already received this request in the past (see request
	   ID), ignore the request. 
           Otherwise, check if the requested file is stored locally (in the 
	   peer's directory of shared files):
	     + If so, send a "file" message to the source peer of the request 
	       and, if necessary, add this source peer to the list 
	       of neighbors of this peer.
             + If not, send a "lookup" message to all neighbors of this peer,
               except the peer that sent this request (that is, the "from" peer
	       as opposed to the "source" peer of the request).
	 */
	void processLookup(StringTokenizer line) {

	    /* to be completed */

	}// processLookup method

    }// LookupThread class
    
    class FileTransferThread extends Thread {

	ServerSocket serverSocket = null;   // TCP listening socket
	Socket clientSocket = null;         // TCP socket to a client
	DataInputStream in = null;          // input stream from client
	DataOutputStream out = null;        // output stream to client
	String request, reply;

	/* this is the implementation of the peer's File Transfer
	   thread, which first creates a listening socket (or welcome
	   socket or server socket) and then continuously waits for
	   connections. For each connection it accepts, the newly
	   created client socket waits for a single request and processes
	   it using the helper method below (and is finally closed).
	*/	
	public void run() {

	    /* to be completed */

	}// run method
	
	/* Process the given request received by the TCP client
	   socket.  This request must be a "get" message (the only
	   command that uses the TCP sockets). If the requested
	   file is stored locally, read its contents (as a String)
	   using the helper method below and send them to the other side
	   in a "fileFound" message. Otherwise, send back a "fileNotFound"
	   message.
	 */
	void process(String request) { 
	    
	    /* to be completed */

	}// process method

	/* Given a File object for a file that we know is stored at this
	   peer, return the contents of the file as a byte array.
	*/
	byte[] readFile(File file) {

	    /* to be completed */

	    return null;
	}// readFile method

	/* Open the necessary I/O streams and initialize the in and out
	   variables; this method does not catch any exceptions.
	*/
	void openStreams() throws IOException {

	    /* to be completed */

	}// openStreams method

	/* close all open I/O streams and the client socket
	 */
	void close() {

	    /* to be completed */

	}// close method
	
    }// FileTransferThread class

}// Peer class
