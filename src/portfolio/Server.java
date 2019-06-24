package portfolio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

//GOALS
//
//1. to show sample Server code
//Note that the server is running on LOCALHOST (which is THIS computer) and the 
//port number associated with this server program is 4444.
//The accept() method just WAITS until some client program tries to access this server
//
//2. to show how a thread is created to handle client request
//

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

	public static void main(String[] args) throws IOException {

		ArrayList<ClientHandler> clients = new ArrayList<>();
		ServerSocket serverSocket = null;
		int clientNum = 0; // keeps track of how many clients were created

		// 1. CREATE A NEW SERVERSOCKET
		try {
			serverSocket = new ServerSocket(4444); // provide MYSERVICE at port
													// 4444
		} catch (IOException e) {
//			System.out.println("Could not listen on port: 4444");
			System.exit(-1);
		}

		// 2. LOOP FOREVER - SERVER IS ALWAYS WAITING TO PROVIDE SERVICE!
		while (true) {
			Socket clientSocket = null;
			try {
				// 2.1 WAIT FOR CLIENT TO TRY TO CONNECT TO SERVER
//				System.out.println("Waiting for client " + clientNum + " to connect!");
				clientSocket = serverSocket.accept();
				ClientHandler c = new ClientHandler(clientSocket, clients, clientNum);
				clientNum++;
				clients.add(c);
				// 2.2 SPAWN A THREAD TO HANDLE CLIENT REQUEST
				Thread t = new Thread(c);
				t.start();

			} catch (IOException e) {
//				System.out.println("Accept failed: 4444");
				System.exit(-1);
			}

			// 2.3 GO BACK TO WAITING FOR OTHER CLIENTS
			// (While the thread that was created handles the connected client's
			// request)

		} // end while loop

	} // end of main method

} // end of class MyServer

class ClientHandler implements Runnable {
	Socket s; // this is socket on the server side that connects to the CLIENT
	ArrayList<ClientHandler> others;
	Scanner in;
	PrintWriter out;
	int clientNum;

	ClientHandler(Socket s, ArrayList<ClientHandler> others, int clientNum) throws IOException {
		this.clientNum = clientNum;
		this.s = s;
		this.others = others;
		in = new Scanner(s.getInputStream());
		out = new PrintWriter(s.getOutputStream());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Listening for messages from client
		while (true) {
			final String clientMessage = in.nextLine();
			new Thread(new Runnable() { // Create new thread to handle message

				@Override
				public void run() {
					//Simulate delay in server communication
//					try {
//						Thread.sleep(250);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					for (ClientHandler c : others) {
						c.sendMessage(clientMessage + " " + clientNum);
					}
				}
			}).start();
		}
	}

	private void sendMessage(String str) {
		out.println(str);
		out.flush();
	}
} // end of class ClientHandler
