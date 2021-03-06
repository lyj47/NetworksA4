
/** Peer.java - A peer-to-peer file sharing program
 *
 *  @version CS 391 - Spring 2018 - A4
 *
 *  @author Jonathan Ly
 *
 *  @author Sean Mitchell
 * 
 *  @bug The initial join of a new peer does work fully. [pick one]
 *
 *  @bug The Status command does work fully. [pick one]
 *
 *  @bug The Find command does work fully. [pick one]
 *
 *  @bug The Get command does work fully. [pick one]
 * 
 *  @bug The Quit command does work fully. [pick one]
 * 
 **/

import java.io.*;
import java.net.*;
import java.util.*;

class Peer {
	String name, // symbolic name of the peer, e.g., "P1" or "P2"
			ip, // IP address in dotted decimal notation, e.g., "127.0.0.1"
			filesPath; // path to local file repository, e.g., "dir1/dir2/dir3"
	int lPort, // lookup port number (permanent UDP port)
			ftPort; // file transfer port number (permanent TCP port)
	List<Neighbor> neighbors; // current neighbor peers of this peer
	LookupThread lThread; // thread listening to the lookup socket
	FileTransferThread ftThread; // thread list. to the file transfer socket
	int seqNumber; // identifier for next Find request (used to
					// control the flooding by avoiding loops)
	Scanner scanner; // used for keyboard input
	HashSet<String> findRequests; // record of all lookup requests seen so far

	/*
	 * Instantiate a new peer (including setting a value of 1 for its initial
	 * sequence number), launch its lookup and file transfer threads, (and start the
	 * GUI thread, which is already implemented for you)
	 */
	Peer(String name2, String ip, int lPort, String filesPath, String nIP, int nPort) {

		if (ip.toLowerCase().trim().equals("localhost"))
			ip = "127.0.0.1";

		File directory = new File(filesPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		try {
			String file_name = "F" + name2.charAt(name2.length() - 1) + ".txt";
			File new_file = new File(directory + "/" + file_name);
			if (!new_file.exists())
				new_file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new_file));
			writer.write("This is example data for the file " + file_name);
			writer.close();

			if (name2.charAt(name2.length() - 1) == 4) {
				String afile_name = "A" + name2.charAt(name2.length() - 1) + ".txt";
				String bfile_name = "B" + name2.charAt(name2.length() - 1) + ".txt";
				new_file = new File(directory + "/" + afile_name);
				writer = new BufferedWriter(new FileWriter(new_file));
				writer.close();
				if (!new_file.exists())
					new_file.createNewFile();
				new_file = new File(directory + "/" + bfile_name);
				writer = new BufferedWriter(new FileWriter(new_file));
				writer.write("This is example data for the file " + afile_name);
				if (!new_file.exists())
					new_file.createNewFile();
				writer.write("This is example data for the file " + bfile_name);
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.name = name2;
		this.filesPath = filesPath;
		this.ip = ip;
		this.seqNumber = 1;

		this.scanner = new Scanner(System.in);
		this.findRequests = new HashSet<String>();
		this.neighbors = new ArrayList<Neighbor>();

		this.lThread = new LookupThread();
		this.lThread.start();
		this.lPort = lPort;

		this.ftThread = new FileTransferThread();
		this.ftThread.start();
		this.ftPort = lPort + 1;

		if (nIP != null && !nIP.equals("") && nPort != 0) {
			String join_request = "init join " + nIP + " " + nPort + " " + ip + " " + lPort;
			try {
				DatagramSocket socket = new DatagramSocket(12348);
				socket.setBroadcast(true);
				DatagramPacket packet = new DatagramPacket(join_request.getBytes(), join_request.getBytes().length,
						InetAddress.getByName(nIP), nPort);
				socket.send(packet);
				socket.setSoTimeout(5000);
				socket.receive(packet);

				if (new String(packet.getData()).contains("okay"))
					neighbors.add(new Neighbor(nIP, nPort));

				socket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("The peer you tried to connect to was unavailable");
			}
		}

		GUI.createAndShowGUI("Peer " + name);
	}// constructor

	/*
	 * display the commands available to the user Do NOT modify this method.
	 */
	static void displayMenu() {
		System.out.println("\nYour options:");
		System.out.println("    1. [S]tatus");
		System.out.println("    2. [F]ind <filename>");
		System.out.println("    3. [G]et <filename> <peer IP> <peer port>");
		System.out.println("    4. [Q]uit");
		System.out.print("Your choice: ");
	}// displayMenu method

	/*
	 * input the next command chosen by the user
	 */
	int getChoice() {
		try {
			String input = scanner.next();

			if (input.toUpperCase().equals("S") || input.equals("1"))
				return 1;
			else if (input.toUpperCase().equals("F") || input.equals("2"))
				return 2;
			else if (input.toUpperCase().equals("G") || input.equals("3"))
				return 3;
			else if (input.toUpperCase().equals("Q") || input.equals("4"))
				return 4;
		} catch (NoSuchElementException e) {
			System.exit(1);
		}
		return -1;
	}// getChoice method

	/*
	 * this is the implementation of the peer's main thread, which continuously
	 * displays the available commands, input the user's choice, and executes the
	 * selected command, until the latter is "Quit"
	 */
	void run() {
		while (true) {
			displayMenu();

			int input = getChoice();

			System.out.println("\n=============== ");

			switch (input) {
			case 1:
				this.processStatusRequest();
				break;
			case 2:
				this.processFindRequest();
				break;
			case 3:
				this.processGetRequest();
				break;
			case 4:
				this.processQuitRequest();
				break;
			default:
				break;
			}
		}
	}// run method

	/*
	 * execute the Quit command, that is, send a "leave" message to all of the
	 * peer's neighbors, then terminate the lookup thread
	 */
	void processQuitRequest() {
		byte[] leave_request = new String("leave " + this.ip + " " + this.lPort).getBytes();

		DatagramPacket packet;
		try {
			DatagramSocket socket = null;

			for (Neighbor n : neighbors) {
				packet = new DatagramPacket(leave_request, leave_request.length, InetAddress.getByName(n.ip), n.port);
				socket = new DatagramSocket();
				socket.setBroadcast(true);
				socket.send(packet);
			}
			socket.close();
			lThread.terminate();
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// processQuitRequest method

	/*
	 * execute the Status command, that is, read and display the list of files
	 * currently stored in the local directory of shared files, then print the list
	 * of neighbors. The EXACT format of the output of this method (including
	 * indentation, line separators, etc.) is specified via examples in the
	 * assignment handout.
	 */
	void processStatusRequest() {
		File local_directory = new File(filesPath);

		System.out.println("Local files:");

		if (local_directory.exists()) {
			for (File file : local_directory.listFiles()) {
				System.out.println("\t" + file.getName());
			}
		}

		this.printNeighbors();

	}// processStatusRequest method

	/*
	 * execute the Find command, that is, prompt the user for the file name, then
	 * look it up in the local directory of shared files. If it is there, inform the
	 * user. Otherwise, send a lookup message to all of the peer's neighbors. The
	 * EXACT format of the output of this method (including the prompt and
	 * notification), as well as the format of the 'lookup' messages are specified
	 * via examples in the assignment handout. Do not forget to handle the
	 * Find-request ID properly.
	 */
	void processFindRequest() {
		scanner.nextLine();
		System.out.print("Name of file to find: ");
		String requested_file = scanner.nextLine().trim();
		String full_request = "lookup " + requested_file + " " + this.name + "#" + this.seqNumber + " " + this.ip + " "
				+ this.lPort;
		boolean isLocalFile = false;

		byte[] data = full_request.getBytes();

		try {
			File local_files = new File(filesPath);

			for (File local_file : local_files.listFiles()) {
				if (local_file.getName().equals(requested_file)) {
					System.out.println("This file exists locally in " + filesPath + "/");
					System.out.println("=============== ");
					isLocalFile = true;
					break;
				}
			}

			if (!isLocalFile) {
				final DatagramSocket socket = new DatagramSocket(null);
				socket.setReuseAddress(true);
				socket.bind(new InetSocketAddress(ip, 12349));
				socket.setSoTimeout(3000);
				socket.setBroadcast(true);
				DatagramPacket packet = null;

				byte[] in_data = new byte[512];
				DatagramPacket in_packet = new DatagramPacket(in_data, in_data.length, InetAddress.getByName(ip),
						12349);

				for (Neighbor n : neighbors) {
					packet = new DatagramPacket(data, data.length, InetAddress.getByName(n.ip), n.port);
					socket.send(packet);
				}
				if (neighbors.size() > 0) {
					socket.receive(in_packet);

					String[] received_data_parts = new String(in_packet.getData()).split(" ");
					String peer_ip = received_data_parts[1].trim();
					String peer_port = received_data_parts[2].trim();

					boolean isNeighbor = false;

					for (Neighbor n : neighbors) {
						if (n.ip.equals(peer_ip) && n.port == Integer.parseInt(peer_port)) {
							isNeighbor = true;
						}
					}

					if (!isNeighbor)
						this.neighbors.add(
								new Neighbor(received_data_parts[1], Integer.parseInt(received_data_parts[2].trim())));
					if (received_data_parts[0].contains("fileFound"))
						GUI.displayLU("Received:\tfile " + requested_file + " is at " + peer_ip + " " + peer_port
								+ " (tcp port:\t" + (Integer.parseInt(peer_port) + 1));
				}
				socket.close();
			}
			this.seqNumber++;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			this.seqNumber++;
		}
	}// processFindRequest method

	/*
	 * execute the Get command, that is, prompt the user for the file name and
	 * address and port number of the selected peer with the needed file. Send a
	 * "get" message to that peer and wait for its response. If the file is not
	 * available at that peer, inform the user. Otherwise, extract the file contents
	 * from the response, output the contents on the user's terminal and save this
	 * file (under its original name) in the local directory of shared files. The
	 * EXACT format of this method's output (including the prompt and notification),
	 * as well as the format of the "get" messages are specified via examples in the
	 * assignment handout.
	 */
	void processGetRequest() {
		scanner.nextLine();
		boolean isLocalFile = false;

		String requested_file = "";
		String destination_ip = "";
		String destination_port = "";

		try {
			System.out.print("Name of file to find: ");
			requested_file = scanner.nextLine();
			System.out.print("Address of source peer: ");
			destination_ip = scanner.nextLine();
			System.out.print("Port of source peer: ");
			destination_port = scanner.nextLine();
		} catch (NoSuchElementException e) {
			this.processQuitRequest();
		}

		String request = requested_file + " " + destination_ip + " " + destination_port;

		File local_files = new File(filesPath);

		for (File local_file : local_files.listFiles()) {
			if (local_file.getName().equals(requested_file)) {
				System.out.println("This file exists locally in " + filesPath + "/");
				isLocalFile = true;
				break;
			}
		}

		DataInputStream in = null;
		DataOutputStream out = null;

		if (!isLocalFile) {
			try {
				Socket client = new Socket(ip, Integer.parseInt(destination_port));

				in = new DataInputStream(client.getInputStream());
				out = new DataOutputStream(client.getOutputStream());

				out.writeUTF(request);

				String response = in.readUTF();

				if (response.contains("fileFound")) {
					writeFile(requested_file, response.substring(10));
					System.out.println(requested_file + ":");
					System.out.println("Contents of the received file between dashes: ");
					System.out.println("---------------------------------- ");
					System.out.println(requested_file + ":");
					System.out.println("\tThis is the whole " + requested_file + " file.");
					System.out.println("---------------------------------- ");
					System.out.println("=============== ");
				} else if (response.contains("fileNotFound")) {
					System.out.println("The file \'" + requested_file + "\' is not available at " + destination_ip + ":"
							+ destination_port);
					System.out.println("=============== ");
				}

			} catch (IOException e) {
				System.out.println("Unable to connect to peer");
			}
		}

	}// processGetRequest method

	/*
	 * create a text file in the local directory of shared files whose name and
	 * contents are given as arguments.
	 */
	void writeFile(String fileName, String contents) {
		try {
			File file = new File(filesPath + "/" + fileName);

			if (!file.exists())
				file.createNewFile();

			FileWriter writer = new FileWriter(file);
			writer.write(contents);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}// writeFile method

	/*
	 * Send to the user's terminal the list of the peer's neighbors. The EXACT
	 * format of this method's output is specified by example in the assignment
	 * handout.
	 */
	void printNeighbors() {
		System.out.println("Neighbors:");
		for (Neighbor n : this.neighbors)
			System.out.println("\t" + n.ip + " " + n.port);
		System.out.println("==================");
	}// printNeighbors method

	/*
	 * Do NOT modify this class
	 */
	class Neighbor {
		String ip;
		int port;

		Neighbor(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}// constructor

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Neighbor)) {
				return false;
			}
			Neighbor n = (Neighbor) o;
			return n.ip.equals(ip) && n.port == port;
		}// equals method

		public String toString() {
			return ip + ":" + port;
		}// toString method
	}// Neighbor class

	class LookupThread extends Thread {

		DatagramSocket socket = null; // UDP server socket
		private volatile boolean stop = false; // flag used to stop the thread

		/*
		 * Stop the lookup thread by closing its server socket. This works (in a
		 * not-so-pretty way) because this thread's run method is constantly listening
		 * on that socket. Do NOT modify this method.
		 */
		public void terminate() {
			stop = true;
			socket.close();
		}// terminate method

		/*
		 * This is the implementation of the thread that listens on the UDP lookup
		 * socket. First (at startup), if the peer has exactly one neighbor, send a
		 * "join" message to this neighbor. Otherwise, skip this step. Second,
		 * continuously wait for an incoming datagram (i.e., a request), display its
		 * contents in the GUI's Lookup panel, and process the request using the helper
		 * method below.
		 */
		public void run() {
			try {
				socket = new DatagramSocket(lPort);
			} catch (SocketException e) {
				e.printStackTrace();
			}

			while (true) {
				try {
					if (socket.isClosed())
						break;
					byte[] b = new byte[512];

					DatagramPacket packet = new DatagramPacket(b, b.length);
					socket.receive(packet);

					String in_request = new String(packet.getData());

					if (in_request.contains("leave")) {
						String ip = in_request.split(" ")[1];
						String port = in_request.split(" ")[2];
						GUI.displayLU("Received:\tleave " + ip + " " + port);
						for (int i = 0; i < neighbors.size(); i++) {
							if (neighbors.get(i).ip.equals(ip)
									&& neighbors.get(i).port == Integer.parseInt(port.trim())) {
								neighbors.remove(i);
							}
						}
					} else
						this.process(in_request);
				} catch (IOException e) {

				}
			}
		}// run method

		/*
		 * This helper method processes the given request, which was received by the
		 * Lookup socket. Based on the first field of the request (i.e., the "join",
		 * "leave", "lookup", or "file" keyword), perform the appropriate action. All
		 * actions are quite short, except for the "lookup" request, which has its own
		 * helper method below.
		 */
		void process(String request) {
			try {
				if (request.contains("join")) {

					String[] request_parts = request.split(" ");

					String source_ip = "";
					String source_port = "";

					if (!request.contains("init")) {
						source_ip = request_parts[3].trim();
						source_port = request_parts[4].trim();
					} else {
						source_ip = request_parts[4].trim();
						source_port = request_parts[5].trim();
					}

					if (request.contains("init")) {
						GUI.displayLU("Received:\tjoin " + source_ip + " " + source_port);

						boolean isNeighbor = false;
						for (Neighbor n : neighbors)
							if (n.ip.equals(source_ip) && n.port == Integer.parseInt(source_port))
								isNeighbor = true;

						if (!isNeighbor)
							neighbors.add(new Neighbor(source_ip.trim(), Integer.parseInt(source_port.trim())));

						String response = "okay";

						DatagramPacket packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
								InetAddress.getByName(source_ip), 12348);
						socket.send(packet);
					}
				} else if (request.contains("lookup")) {
					processLookup(new StringTokenizer(request.substring(6), " "));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}// process method

		/*
		 * This helper method processes a "lookup" request received by the Lookup
		 * socket. This request is represented by the given tokenizer, which contains
		 * the whole request line, minus the "lookup" keyword (which was removed from
		 * the beginning of the request) by the caller of this method. Here is the
		 * algorithm to process such requests: If the peer already received this request
		 * in the past (see request ID), ignore the request. Otherwise, check if the
		 * requested file is stored locally (in the peer's directory of shared files): +
		 * If so, send a "file" message to the source peer of the request and, if
		 * necessary, add this source peer to the list of neighbors of this peer. + If
		 * not, send a "lookup" message to all neighbors of this peer, except the peer
		 * that sent this request (that is, the "from" peer as opposed to the "source"
		 * peer of the request).
		 */
		void processLookup(StringTokenizer line) {
			String file_name = line.nextToken().trim();
			String seq_number = line.nextToken().trim();
			String source_ip = line.nextToken().trim();
			String source_port = line.nextToken().trim();
			String lookup_port = "-1";

			String full_request = "lookup " + file_name + " " + seq_number + " " + source_ip + " " + source_port;

			if (line.hasMoreTokens()) {
				line.nextToken();
				lookup_port = line.nextToken().trim();
			}

			byte[] data = full_request.getBytes();

			if (findRequests.contains(seq_number)) {
				GUI.displayLU("Received:\t" + full_request);
				GUI.displayLU("Saw request before:\tno forwarding");
			} else {
				findRequests.add(seq_number);
				File directory = new File(filesPath);

				boolean hasFoundFile = false;
				String response;

				for (File file : directory.listFiles()) {
					if (file.getName().equals(file_name)) {

						boolean isNeighbor = false;

						for (Neighbor n : neighbors)
							if (n.ip.equals(source_ip) && n.port == Integer.parseInt(source_port))
								isNeighbor = true;

						if (!isNeighbor)
							neighbors.add(new Neighbor(source_ip.trim(), Integer.parseInt(source_port.trim())));

						response = "fileFound " + ip + " " + lPort + " " + source_ip + " " + source_port;

						GUI.displayLU("Received:\tlookup " + file_name + " " + seq_number + " " + source_ip + " "
								+ source_port);
						GUI.displayLU("Sent:\tfile " + file_name + " is at " + ip + " " + lPort + " (tcp port:\t"
								+ ftPort + ")");
						DatagramPacket packet;
						try {
							packet = new DatagramPacket(response.getBytes(), response.getBytes().length,
									InetAddress.getByName(source_ip), 12349);
							socket.send(packet);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						hasFoundFile = true;
						break;
					}
				}

				if (!hasFoundFile) {
					GUI.displayLU("Received:\t" + full_request);
					GUI.displayLU("\tFile is NOT available at this peer");
					for (Neighbor n : neighbors) {
						DatagramPacket packet;
						try {
							if (n.port != Integer.parseInt(source_port)) {
								if (n.port != Integer.parseInt(lookup_port)) {
									GUI.displayLU("\tForward request to " + n.ip + ":" + n.port);
									full_request += " " + ip + " " + lPort;
									data = full_request.getBytes();
									packet = new DatagramPacket(data, data.length, new InetSocketAddress(n.ip, n.port));
									socket.send(packet);
								}
							}
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}// processLookup method
	}// LookupThread class

	class FileTransferThread extends Thread {

		ServerSocket serverSocket = null; // TCP listening socket
		Socket clientSocket = null; // TCP socket to a client
		DataInputStream in = null; // input stream from client
		DataOutputStream out = null; // output stream to client
		String request, reply;

		/*
		 * this is the implementation of the peer's File Transfer thread, which first
		 * creates a listening socket (or welcome socket or server socket) and then
		 * continuously waits for connections. For each connection it accepts, the newly
		 * created client socket waits for a single request and processes it using the
		 * helper method below (and is finally closed).
		 */
		public void run() {
			request = "";
			reply = "";

			try {
				this.openStreams();
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true) {
				try {
					this.clientSocket = serverSocket.accept();
					this.out = new DataOutputStream(clientSocket.getOutputStream());
					this.in = new DataInputStream(clientSocket.getInputStream());
					request = in.readUTF();

					process(request);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}// run method

		/*
		 * Process the given request received by the TCP client socket. This request
		 * must be a "get" message (the only command that uses the TCP sockets). If the
		 * requested file is stored locally, read its contents (as a String) using the
		 * helper method below and send them to the other side in a "fileFound" message.
		 * Otherwise, send back a "fileNotFound" message.
		 */
		void process(String request) {
			String[] request_parts = request.split(" ");

			File file = new File(filesPath + "/" + request_parts[0]);

			try {
				if (file.exists()) {
					reply = "fileFound " + new String(readFile(file));
					GUI.displayFT("Received:\tget " + request_parts[0]);
					GUI.displayFT("\tRead file " + file.getPath());
					GUI.displayFT("\tSent back fie contents");
				} else {
					reply = "fileNotFound";
					GUI.displayFT("Received:\tget " + request_parts[0]);
					GUI.displayFT("\tresponded: " + reply);
				}
				out.writeUTF(reply);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}// process method

		/*
		 * Given a File object for a file that we know is stored at this peer, return
		 * the contents of the file as a byte array.
		 */
		byte[] readFile(File file) {
			try {
				FileInputStream stream = new FileInputStream(file);

				byte[] file_content = new byte[(int) file.length()];

				stream.read(file_content);

				stream.close();

				return file_content;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}// readFile method

		/*
		 * Open the necessary I/O streams and initialize the in and out variables; this
		 * method does not catch any exceptions.
		 */
		void openStreams() throws IOException {
			this.serverSocket = new ServerSocket(ftPort);
		}// openStreams method

		/*
		 * close all open I/O streams and the client socket
		 */
		void close() {
			try {
				this.out.close();
				this.in.close();
				this.serverSocket.close();
				this.clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}// close method
	}// FileTransferThread class
}// Peer class
