package TextboardServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import TextboardServer.Textboard.Message;


/**
 * Bind a port, create a socket and communicate with clients. Default Port is 3141.
 * @author Michael Ratke and Timon Sachweh
 *
 */
public class Server {
	ServerSocket serverSocket;
	Textboard textboard;
	boolean terminate;
	int countClients = 1;

	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	
	public Server(int port) {
		terminate = false;
		textboard = new Textboard();

		try {
			serverSocket = new ServerSocket(port); // Bind the port
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Create server socket on port: " + port);
		} catch (IOException e) {
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Could not create server socket on port: " + port);
			System.exit(-1);
		}

		textboard.add(System.currentTimeMillis(), "Test", "Testnachricht"); // Create a test message

		// Listen for Clients. Create a new Socket and Thread, if a client create a connection to server.
		while (!terminate) {
			try {
				Socket clientSocket = serverSocket.accept(); // Listen for clients
				new HandleConnection(clientSocket, textboard, countClients++).start(); // New Thread for connection 
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" Connect to Client: " + clientSocket.getInetAddress());
			} catch (IOException e) {
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" Coudn't connect to Client: " + e.getMessage());
			}
		}

		try {
			serverSocket.close(); // Try to close the server socket
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Server Stopped");
		} catch (Exception e) {
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Error Found stopping server socket");
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			throw new IllegalArgumentException("Too many argumants!");
		}

		try {
			new Server(Integer.parseInt(args[0]));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Format of argumant is not Integer!");
		} catch (ArrayIndexOutOfBoundsException e) {
			new Server(3141);
		}
	}
	
	
	

	/**
	 * Handle the communication between Server and Client (Thread)
	 * @author Michael Ratke and Timon Sachweh
	 *
	 */
	private class HandleConnection extends Thread implements Observer {
		Textboard textboard;
		Socket clientSocket = null;
		BufferedReader input = null;
		PrintStream output = null;
		boolean terminate = false;

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");	// Date-Format fuer Zeitstempel

		/**
		 * Constructor for a new Client.
		 * @param socket Client socket for communication.
		 * @param textboard List of messages.
		 * @param numberOfClient Number of this Thread.
		 */
		public HandleConnection(Socket socket, Textboard textboard, int numberOfClient) {
			setName("Client " + numberOfClient);
			this.clientSocket = socket;
			this.textboard = textboard;
			try {
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Open an Inputstream
				this.output = new PrintStream(clientSocket.getOutputStream(), true);	// Open an Outputstream
			} catch (IOException e) {
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" " + getName() + ": Couldn`t create an Input- or Outputstream");
			}
			output.println("Wellcome. You're the " + getName());
			this.textboard.addObserver(this); // Add this Thread as an Observer for handling new messages
		}

		/**
		 * Terminates this client thread.
		 */
		public void terminate() {
			try {
				input.close();
				clientSocket.close();
			} catch (IOException e) {
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" " + getName() + ": Cound't close socket or inputstream");
			}
			output.close();
			terminate = true;
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" " + getName() + ": Terminate Socket");
		}

		/**
		 * Listens and processes commands when the client reports.
		 * Command <W> <Time> returns a sublist of all messages by time.
		 * Command <P> write new messages in the list and notify all open clients about this messages
		 * Command <T> <Topic> returns a sublist of all messages by selected topic.
		 * Command <L> <Count> returns a sublist of all last edited messages by count.
		 * Command <X> close the socket.
		 */
		public void run() {
			while (!terminate) {
				if (clientSocket != null && output != null && input != null) {
					try {
						String firstLine = input.readLine();
						System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
								" " + getName() +
								": " + firstLine);
						String[] splitFirstLine = firstLine.split(" ");
						String part1 = splitFirstLine[0];
						String part2 = null;
						if (splitFirstLine.length == 2) {
							part2 = splitFirstLine[1];
						}
						long time = 0;
// Command <W>
						if (part1.equals("W")) {
							try {
								time = Long.parseLong(splitFirstLine[1]); // Try to parse the time
							} catch (NumberFormatException e) {
								output.println("E " + e.getMessage());
							}
							List<Message> sublistByTime = textboard.getByTime(time); // Sublist of messages by time
							if (sublistByTime != null) {
								output.println(sublistByTime.size());
								for(int i = 0; i < sublistByTime.size(); i++) {
									output.println(sublistByTime.get(i).toString()); // Send the messages to the client
								}
							} else {
								output.println(0);
							}
// Command <P>
						} else if (part1.equals("P")) {
							String line = input.readLine();
							int numberOfMessages = 0;

							try {
								numberOfMessages = Integer.parseInt(line); // Parse the number of new messages
							} catch (NumberFormatException e) {
								System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
										" " + getName() + ": Couldn`t parse the number of messages");
								output.println("E Couldn`t parse the number of messages");
								break;
							}

							int allNewMessages = numberOfMessages; // Counter for the Observer

							while (numberOfMessages > 0) {
								line = input.readLine(); // Read row count
								int length = 0;

								try {
									length = Integer.parseInt(line) - 1; // Try to parse the row count
								} catch (NumberFormatException e) {
									System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
											" " + getName() + ": Couldn`t parse the count of lines");
									output.println("E Couldn`t parse the count of lines");
									break;
								}

								line = input.readLine(); // Read the time and topic
								String[] splitLine = line.split(" "); // and split the row
								time = System.currentTimeMillis();
								String topic = "";

								if (splitLine.length > 1) {
									topic = splitLine[1];
									for (int i = 2; i < splitLine.length; i++) { // Merge the topic
										topic += " " + splitLine[i];
									}
								} else {
									System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
											" " + getName() + ":  No topic available");
									output.println("E No topic available");
									break;
								}

								String message = "";
								while (length != 0) {	// Merge the message
									if (message.isEmpty()) {
										message += input.readLine();
									} else {
										message += "\n" + input.readLine();
									}
									length--;
								}

								textboard.add(time, topic, message); // Add the message to the textboard list

								numberOfMessages--;
							}

							textboard.createChangeOfObserver(allNewMessages); // Set the number of new messages
							textboard.notifyObservers();

// Command <T>
						} else if (part1.equals("T")) {
							List<Message> sublistByTopic = textboard.getByTopic(part2); 	// Sublist of messages by topic
							if (sublistByTopic != null) {
								output.println(sublistByTopic.size());
								for(int i = 0; i < sublistByTopic.size(); i++) {
									output.println(sublistByTopic.get(i).toString());	// Send the message to client
								}
							} else {
								output.println(0);
							}
// Command <L>
						} else if (part1.equals("L")) {
							int index = 0;
							try {
								index = Integer.parseInt(part2);	// Try to parse the count of last edited messages
							} catch (NumberFormatException e) {
								System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
										" " + getName() + ":  Couldn`t parse the index for sublistByCount");
								output.println("E Couldn`t parse the index for sublistByCount");
							}
							try {
								List<Message> sublistByCount = textboard.getLastEdited(index);	// Sublist of messages by last edit
								if (sublistByCount.size() > 0) {
									output.println(sublistByCount.size());
									for(int i = 0; i < sublistByCount.size(); i++) {
										output.println(sublistByCount.get(i).getTime() +
												" " + sublistByCount.get(i).getTopic());	// Send the messages to client
									}
								} else {
									output.println(0);
								}
							} catch (IndexOutOfBoundsException e) {
								output.println("E " + e.getMessage());
							}
// Command <X>
						} else if (part1.equals("X")) {
							output.println("Server close this Socket. Please disconnect communication!");
							terminate();
						} else {
							System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
									" " + getName() + ": Protocol error. Couldn't match command");
							output.println("E Protocol error. Couldn't match command");
						}
					} catch (IOException e) {
						output.println("E " + e.getMessage());
					} catch (NullPointerException e) {
						System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
								" " + getName() + ": " + e.getMessage());
						terminate();
					}
				} else {	// Terminate the thread if connection is lost
					System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
							" " + getName() + ": Connection lost");
					terminate();
				}
			}
		}

		/**
		 * 
		 * @param o
		 * @param arg
		 */
		@Override
		public void update(Observable o, Object arg) {

			int countUpdatedMessages = textboard.getCounterNewMessages();
			ArrayList<Message> newMessages = textboard.lastMessages();

			//Number of messages
			String returnMessage = "N " + countUpdatedMessages + "\n";

			//time and topic from each message
			for(int i = (newMessages.size()-1); i >= 0; i--){
				if (i > 0) {
					returnMessage = returnMessage +
							newMessages.get(i).getTime() + " " +
							newMessages.get(i).getTopic() + "\n";
				} else {
					returnMessage = returnMessage +
							newMessages.get(i).getTime() + " " +
							newMessages.get(i).getTopic();
				}
			}
			
			this.output.println(returnMessage);
		}
	}
}
