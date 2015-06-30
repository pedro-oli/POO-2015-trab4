import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

public class Client {
	// reads input from Console:
	private BufferedReader br;
	// socket info:
	private int iPort;
	private String strIP;
	// used to check if LogIn is valid:
	private boolean logInValid;
	// used to check if the purchase was successful
	private boolean purchaseValid;

	// main
	public static void main(String[] args) throws IOException {
		new Client().init();
	}

	// Client constructor
	public Client() {
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.strIP = new String();
		this.logInValid = false;
		this.purchaseValid = false;
	}

	// Receiver class
	class Receiver implements Runnable {
		private InputStream inputstream;

		public Receiver(InputStream inputstream) {
			this.inputstream = inputstream;
		}

		public void run() {
			Scanner scanner = new Scanner(this.inputstream);
			while(scanner.hasNextLine()) {
				String strServerInput = scanner.nextLine();
				if(strServerInput.equals("logInValid")) {
					logInValid = true;
				}
				else if(strServerInput.equals("purchaseValid")) {
					purchaseValid = true;
				}
				else {
					System.out.println(strServerInput);
				}
			}
		}
	}

	// "clears" Console
	public void clearConsole() {
		for (int i = 0; i < 150; i++) {
    		System.out.println();
        }
    }

    // runs the Client
	public void init() throws IOException {
		boolean valid = false;
		Socket socket = new Socket();

		// connects to a Server
		while(!valid) {
			try {
				// gets IP and Port number
				System.out.println("Enter IP:");
				this.strIP = br.readLine();
				System.out.println("Enter port number:");
				this.iPort = Integer.parseInt(br.readLine());

				// creates Socket
				socket = new Socket(this.strIP, this.iPort);
				valid = true;
			}
			catch(Exception e) {
				clearConsole();
				System.out.println("Invalid input! Try again.");
			}
		}

		// creates the Receiver
		Receiver receiver = new Receiver(socket.getInputStream());
		new Thread(receiver).start();
		
		// shows Client menu
		clearConsole();
		PrintStream printstream = new PrintStream(socket.getOutputStream());
		boolean quit = false;
		String strClientInput = new String();
		while(!quit) {
			try {
				System.out.println("***************Client Menu**************");
				System.out.println("*     1 - Register new client          *");
				System.out.println("*     2 - Show all products            *");
				System.out.println("*     3 - Log in and make a purchase   *");
				System.out.println("*     4 - Exit                         *");
				System.out.println("****************************************");
				// reads input
				strClientInput = br.readLine();

				// registers new Client
				if(strClientInput.equals("1")) {
					signUp(printstream);
				}

				// shows all products
				else if(strClientInput.equals("2")) {
					showProducts(printstream);
				}

				// logs in with existing Client and makes a purchase
				else if(strClientInput.equals("3")) {
					logInAndPurchase(printstream);
				}

				// exit program
				else if(strClientInput.equals("4")) {
					quit = true;
				}
				else {
					System.out.println("Invalid option! Try again.");
					clearConsole();
				}
			}
			catch(IOException e) {
				clearConsole();
				System.out.println("Invalid input! Try again.");
				System.out.println();
			}
		}

		printstream.close();	// closes the Socket's OutputStream
		socket.close();			// closes the Socket
	}

	// registers a new Client
	public void signUp(PrintStream ps) {
		clearConsole();

		// sends request to the Server
		ps.println("signUp");

		// gets new User info:
		try {
			System.out.println("Enter Name:");
			String userName = br.readLine();
			System.out.println("Enter Address:");
			String userAddress = br.readLine();
			System.out.println("Enter Phone:");
			String userPhone = br.readLine();
			System.out.println("Enter Email:");
			String userEmail = br.readLine();
			System.out.println("Enter Password:");
			String userPassword = br.readLine();

			// sends new User's info to the Server
			ps.println(userName);
			ps.println(userAddress);
			ps.println(userPhone);
			ps.println(userEmail);
			ps.println(userPassword);

			// just a small delay to ensure the Server gets the new User's info before the next step
			try {
			    Thread.sleep(2000);
			}
			catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		catch(Exception e) {}

		// returns to the menu
		System.out.println("Please press Enter to return to the menu.");
		try {
			br.readLine();
			clearConsole();
		}
		catch(IOException e) {}
	}

	// gets all Products from the Server
	public void showProducts(PrintStream ps) {
		clearConsole();

		// sends request to the Server
		ps.println("showProducts");

		// just a small delay to ensure the Server shows all products before the next step
		try {
		    Thread.sleep(2000);
		}
		catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		// returns to the menu
		System.out.println("Please press Enter to return to the menu.");
		try {
			br.readLine();
			clearConsole();
		}
		catch(IOException e) {}		
	}

	// logs in and purchases a Product
	public void logInAndPurchase(PrintStream ps) {
		clearConsole();

		// logIn:
		try {
			String clientID = new String();
			String clientPassword = new String();
			while(!logInValid) {
				// sends request to the Server
				ps.println("logIn");

				// gets Client log info
				System.out.println("Enter Client's ID:");
				clientID = br.readLine();
				System.out.println("Enter Password:");
				clientPassword = br.readLine();

				// sends Client log info to the Server
				ps.println(clientID);
				ps.println(clientPassword);

				// just a small delay to ensure the Server gets the Client log info before the next step
				try {
				    Thread.sleep(2000);
				}
				catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				if(!logInValid) {
					clearConsole();
					System.out.println("Client's ID or Password is wrong! Please try again.");
				}
			}

			// resets logInValid
			logInValid = false;

			clearConsole();
			System.out.println("Login was successful!");
		}
		catch(Exception e) {}

		// AndPurchase:
		try{
			String productProvider = new String();
			String productName = new String();
			while(!purchaseValid) {
				// gets Product info
				System.out.println("Enter desired product's provider:");
				productProvider = br.readLine();
				System.out.println("Enter desired product's name:");
				productName = br.readLine();

				// sends Product info to the Server
				ps.println(productProvider);
				ps.println(productName);

				// just a small delay to ensure the Server gets the Product info before the next step
				try {
				    Thread.sleep(2000);
				}
				catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				if(!purchaseValid) {
					clearConsole();
					System.out.println("There are no Products with such information! Please try again.");
				}
			}

			// resets purchaseValid
			purchaseValid = false;

			System.out.println("Purchase was successful!");
		}
		catch(Exception e) {}

		// returns to the menu
		System.out.println("Please press Enter to return to the menu.");
		try {
			br.readLine();
			clearConsole();
		}
		catch(IOException e) {}
	}
}
