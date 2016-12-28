package TextboardServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import TextboardServer.Textboard.Message;

public class Server {
	ServerSocket serverSocket;
	Textboard textboard;
	boolean terminate = false;

	public Server(int port) {
		terminate = false;
		textboard = new Textboard();

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not create server socket on port " + port + ". Quitting.");
			System.exit(-1);
		}

		while (!terminate) {
			try {
				Socket clientSocket = serverSocket.accept();
				new HandleConnection(clientSocket, textboard).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			serverSocket.close();
			System.out.println("Server Stopped");
		} catch (Exception e) {
			System.out.println("Error Found stopping server socket");
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

	private class HandleConnection extends Thread {
		Textboard textboard;
		Socket clientSocket = null;
		BufferedReader input = null;
		PrintStream output = null;
		boolean terminate = false;

		public HandleConnection(Socket socket, Textboard textboard) {
			this.clientSocket = socket;
			this.textboard = textboard;
			try {
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				this.output = new PrintStream(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		}

		public void run() {
			while (!terminate) {
				if (clientSocket != null && output != null && input != null) {
					try {
						String firstLine = input.readLine();
						String[] splitFirstLine = firstLine.split(" ");
						String part1 = splitFirstLine[0];
						String part2 = null;
						if (splitFirstLine.length == 2) {
							part2 = splitFirstLine[1];
						}
						long time = 0;
						
						switch (part1) {
						case "W":
							try {
								time = Long.parseLong(splitFirstLine[1]);
							} catch (IllegalArgumentException e) {
								output.println("E <" + e.getMessage() + ">");
								break;
							}
							Message[] sublistByTime = textboard.getByTime(time);
							if (sublistByTime.length > 0) {
								for(int i = 0; i < sublistByTime.length; i++) {
									output.println(sublistByTime[i].getTime() +
											" " +
											sublistByTime[i].getTopic());
								}
							} else {
								output.println("Keine Nachrichten vorhanden");
							}		
							break;
						case "P":
							String line = input.readLine();
							int numberOfMessages = Integer.parseInt(line);

							while (numberOfMessages > 0) {
								line = input.readLine(); // <Zeilenanzahl> auslesen
								int length = 0;
								try {
									length = Integer.parseInt(line) - 1; // <Zeilenanzahl> parsen
								} catch (Exception err) {
									output.println("E <Fehler bei der Zeilenanzahl>");
									output.flush();
									break;
								}
								line = input.readLine(); // <Zeitpunkt> und <Thema> auslesen
								String[] splitLine = line.split(" "); // und die Zeile splitten
								String topic = "";
								try {
									time = Long.parseLong(splitLine[0]);	// <Zeitpunkt> parsen
									topic = splitLine[1];
								} catch (IllegalArgumentException e) {
									output.println("E <Fehler bei dem Zeitpunkt>");
									output.flush();
									break;
								} catch (NullPointerException e) {
									output.println("E <Thema nicht vorhanden>");
									output.flush();
									break;
								}
								String message = "";
								while (length != 0) {
									message += input.readLine();
									length--;
								}
								textboard.add(time, topic, message);
								System.out.println(textboard.getLastEdited(1)[0].toString());
								numberOfMessages--;
							}
							break;
						case "T":
							Message[] sublistByTopic = textboard.getByTopic(part2);
							if (sublistByTopic.length > 0) {
								for(int i = 0; i < sublistByTopic.length; i++) {
									output.println(sublistByTopic[i].getTime() +
											" " +
											sublistByTopic[i].getTopic());
								}
							} else {
								output.println("Keine Nachrichten vorhanden");
							}
							break;
						case "L":
							int index = 0;
							try {
								index = Integer.parseInt(part2);
								System.out.println("L " + index);
							} catch (IllegalArgumentException e) {
								output.println("E <" + e.getMessage() + ">");
							}
							try {
								Message[] sublistByCount = textboard.getLastEdited(index);
								if (sublistByCount.length > 0) {
									for(int i = 0; i < sublistByCount.length; i++) {
										output.println(sublistByCount[i].getTime() +
												" " +
												sublistByCount[i].getTopic());
									}
								} else {
									output.println("Keine Nachrichten vorhanden");
								}
							} catch (IndexOutOfBoundsException e) {
								output.println(e.getMessage());
							}
							break;
						case "X":
							terminate();
							break;
						default:
							output.println("Protokollfehler");
							terminate();
						}
					} catch (Exception e) {
						output.println(e.getMessage());
						terminate();
					}
				}
			}
		}
	}
}
