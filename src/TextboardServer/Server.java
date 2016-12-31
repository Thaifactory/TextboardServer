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

public class Server {
	ServerSocket serverSocket;
	Textboard textboard;
	boolean terminate = false;
	int countClients = 1;

	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	public Server(int port) {
		terminate = false;
		textboard = new Textboard();

		try {
			serverSocket = new ServerSocket(port);
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Create server socket on port: " + port);
		} catch (IOException e) {
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" Could not create server socket on port: " + port);
			System.exit(-1);
		}

		textboard.add(1, "Test", "Testnachricht");

		while (!terminate) {
			try {
				Socket clientSocket = serverSocket.accept();
				new HandleConnection(clientSocket, textboard, ("Client " + countClients++)).start();
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" Connect to Client: " + clientSocket.getInetAddress());
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" Number of Messages: " + textboard.getSize());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			serverSocket.close();
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
	 * Handle the communication between Server and Client
	 * @author micha
	 *
	 */
	private class HandleConnection extends Thread implements Observer {
		Textboard textboard;
		String name = null;
		Socket clientSocket = null;
		BufferedReader input = null;
		PrintStream output = null;
		boolean terminate = false;

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");	// Date-Format fuer Zeitstempel

		public HandleConnection(Socket socket, Textboard textboard, String name) {
			this.name = name;
			this.clientSocket = socket;
			this.textboard = textboard;
			try {
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				this.output = new PrintStream(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
						" " + name +
						": Couldn`t create an Input- or Outputstream");
			}
			this.textboard.addObserver(this);
		}

		public void terminate() {
			try {
				input.close();
				clientSocket.close();
			} catch (IOException e) {
				output.println(e.getMessage());
				e.printStackTrace();
			}
			output.close();
			terminate = true;
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
					" " + name +
					": Terminate Socket");
		}

		public void run() {
			while (!terminate) {
				if (clientSocket != null && output != null && input != null) {
					try {
						String firstLine = input.readLine();
						System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
								" " + name +
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
								time = Long.parseLong(splitFirstLine[1]);
							} catch (NumberFormatException e) {
								output.println("E <" + e.getMessage() + ">");
							}
							List<Message> sublistByTime = textboard.getByTime(time);
							if (sublistByTime.size() > 0) {
								output.println(sublistByTime.size());
								for(int i = 0; i < sublistByTime.size(); i++) {
									output.println(sublistByTime.get(i).toString());
								}
							} else {
								output.println(0);
							}
// Command <P>
						} else if (part1.equals("P")) {
							String line = input.readLine();
							int numberOfMessages = 0;

							try {
								numberOfMessages = Integer.parseInt(line);
							} catch (NumberFormatException e) {
								System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
										" " + name +
										": Couldn`t parse the number of messages");
								output.println("E <Couldn`t parse the count of messages>");
								break;
							}

							int allNewMessages = numberOfMessages;

							while (numberOfMessages > 0) {
								line = input.readLine(); // <Zeilenanzahl> auslesen
								int length = 0;

								try {
									length = Integer.parseInt(line) - 1; // <Zeilenanzahl> parsen
								} catch (NumberFormatException e) {
									System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
											" " + name +
											": Couldn`t parse the count of lines");
									output.println("E <Couldn`t parse the count of lines>");
									break;
								}

								line = input.readLine(); // <Zeitpunkt> und <Thema> auslesen
								String[] splitLine = line.split(" "); // und die Zeile splitten
								time = System.currentTimeMillis();
								String topic = "";

								if (splitLine.length > 1) {
									for (int i = 1; i < splitLine.length; i++) { // <Tehma> zusammenfuegen
										topic += " " + splitLine[i];
									}
								} else {
									System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
											" " + name +
											":  Couldn`t parse the topic for message");
									output.println("E <Couldn`t parse the topic for message>");
									break;
								}

								String message = "";
								while (length != 0) {
									if (message.isEmpty()) {
										message += input.readLine();
									} else {
										message += "\n" + input.readLine();
									}
									length--;
								}

								textboard.add(time, topic, message);

								numberOfMessages--;
							}

							textboard.createChangeOfObserver(allNewMessages);
							textboard.notifyObservers();

// Command <T>
						} else if (part1.equals("T")) {
							List<Message> sublistByTopic = textboard.getByTopic(part2);
							if (sublistByTopic.size() > 0) {
								output.println(sublistByTopic.size());
								for(int i = 0; i < sublistByTopic.size(); i++) {
									output.println(sublistByTopic.get(i).toString());
								}
							} else {
								output.println(0);
							}
// Command <L>
						} else if (part1.equals("L")) {
							int index = 0;
							try {
								index = Integer.parseInt(part2);
							} catch (NumberFormatException e) {
								System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
										" " + name +
										":  Couldn`t parse the index for sublistByCount");
								output.println("E <Couldn`t parse the index for sublistByCount>");
							}
							try {
								List<Message> sublistByCount = textboard.getLastEdited(index);
								if (sublistByCount.size() > 0) {
									output.println(sublistByCount.size());
									for(int i = 0; i < sublistByCount.size(); i++) {
										output.println(sublistByCount.get(i).getTime() +
												" " +
												sublistByCount.get(i).getTopic());
									}
								} else {
									output.println(0);
								}
							} catch (IndexOutOfBoundsException e) {
								output.println(e.getMessage());
							}
// Command <X>
						} else if (part1.equals("X")) {
							terminate();
						} else {
							System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
									" " + name +
									": Protocol error. Couldn't match command");
							output.println("E <Protocol error. Couldn't match command>");
						}
						//terminate();
					} catch (IOException e) {
						output.println("E <" + e.getMessage() + ">");
						//terminate();
					}

				} else {
					System.out.println(sdf.format(new Date(System.currentTimeMillis())) +
							" " + name +
							": Connection lost");
					terminate();
				}
			}
		}

		@Override
		public void update(Observable o, Object arg) {

			int countUpdatedMessages = textboard.getCounterNewMessages();
			ArrayList<Message> newMessages = textboard.lastMessages();

			//Number of messages
			String returnMessage = "N " + countUpdatedMessages + "\n";

			//time and topic from each message
			for(int i = (newMessages.size()-1); i >= 0; i--){
				returnMessage = returnMessage +
								newMessages.get(i).getTime() + " " +
								newMessages.get(i).getTopic() + "\n";
			}

			this.output.println(returnMessage);
		}
	}
}
