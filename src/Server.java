import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	ServerSocket serverSocket;
	boolean terminate = false;

	public Server(int port) {
		terminate = false;

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not create server socket on port " + port + ". Quitting.");
			System.exit(-1);
		}

		while (!terminate) {
			try {
				Socket clientSocket = serverSocket.accept();
				new HandleConnection(clientSocket).start();
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
		}
	}

	private class HandleConnection extends Thread {
		Socket clientSocket = null;
		BufferedReader input = null;
		PrintStream output = null;
		boolean terminate = false;

		public HandleConnection(Socket socket) {
			this.clientSocket = socket;
			try {
				this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				this.output = new PrintStream(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void terminate() {
			terminate = true;
		}

		public void run() {
			while (!terminate) {
				if (clientSocket != null && output != null && input != null) {
					String firstLine = input.readLine();
					String[] splitLine = firstLine.split(" ");
					String part1 = splitLine[0];
					String part2 = null;
					if (splitLine.length == 2) {
						part2 = splitLine[1];
					}
					
					switch(part1) {
					case "W":
						break;
					case "P":
						break;
					case "T":
						break;
					case "L":
						break;
					default:
						
					}
					
				}
			}
		}
	}
}
