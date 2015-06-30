import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

public class Server {
	private BufferedReader br;
	private static List<User> users;
	private static List<Product> products;

	public static void main(String[] args) throws IOException {
		new Server().init();	// runs Server
		updateToCSV();			// updates CSV from Lists data
		System.exit(0);			// closes program
	}

	public Server() {
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.users = new ArrayList<User>();
		this.products = new ArrayList<Product>();
	}

	class Product {
		private String productName;
		private String productPrice;
		private String productBestBefore;
		private String productProvider;

		public Product(String productName, String productPrice, String productBestBefore, String productProvider) {
			this.productName = productName;
			this.productPrice = productPrice;
			this.productBestBefore = productBestBefore;
			this.productProvider = productProvider;
		}

		public String getName() {
			return this.productName;
		}

		public String getPrice() {
			return this.productPrice;
		}

		public String getBestBefore() {
			return this.productBestBefore;
		}

		public String getProvider() {
			return this.productProvider;
		}
	}

	class User {
		private String userName;
		private String userAddress;
		private String userPhone;
		private String userEmail;
		private int userID;
		private String userPassword;		

		public User(String userName, String userAddress, String userPhone, String userEmail, int userID, String userPassword) {
			this.userName = userName;
			this.userAddress = userAddress;
			this.userPhone = userPhone;
			this.userEmail = userEmail;
			this.userID = userID;
			this.userPassword = userPassword;
		}

		public String getName() {
			return this.userName;
		}

		public String getAddress() {
			return this.userAddress;
		}

		public String getPhone() {
			return this.userPhone;
		}

		public String getEmail() {
			return this.userEmail;
		}

		public int getID() {
			return this.userID;
		}

		public String getPassword() {
			return this.userPassword;
		}
	}

	class DealWithClient implements Runnable {
		private InputStream inputstream;
		private PrintStream printstream;
		private Server server;

		public DealWithClient(InputStream inputstream, PrintStream printstream, Server server) {
			this.inputstream = inputstream;
			this.printstream = printstream;
			this.server = server;
		}

		public void run() {
			Scanner scanner = new Scanner(this.inputstream);
			String strClientInput = new String();
			while(scanner.hasNextLine()) {
				strClientInput = scanner.nextLine();

				// Client is creating a new User:
				if(strClientInput.equals("signUp")) {
					String userName = scanner.nextLine();
					String userAddress = scanner.nextLine();
					String userPhone = scanner.nextLine();
					String userEmail = scanner.nextLine();
					String userPassword = scanner.nextLine();

					try {
						// new User's userID = 1 + last User
						int userID = 1 + server.users.get(server.users.size() - 1).getID();

						User newUser = new User(userName, userAddress, userPhone, userEmail, userID, userPassword);
	                	server.users.add(newUser);

	                	// tells Client that the signUp was successful (if no Exception was thrown)
	                	printstream.println("New User created! ID = " + userID + "\n");
					}
					catch(Exception e) {
						printstream.println("Error creating new User! Please try again.");
					}
				}

				// Client wants to see all Products:
				else if(strClientInput.equals("showProducts")) {
					try {
						printstream.println("-------------------------------------");
						printstream.println("Name | Price | Best Before | Provider");
						for(Product p : server.products) {
							printstream.println(p.getName() + " | " + p.getPrice() + " | " + p.getBestBefore() + " | " + p.getProvider());
						}
						printstream.println("-------------------------------------");
					}
					catch(Exception e) {
						printstream.println("Error showing all Products! Please try again.");
					}
				}

				// Client wants to log in and purchase a Product:
				else if(strClientInput.equals("logIn")) {
					String strClientID = scanner.nextLine();
					String clientPassword = scanner.nextLine();

					try {
						int intClientID = Integer.parseInt(strClientID);
						if(validClient(intClientID, clientPassword)) {
							// tells Client that the log info is valid
							printstream.println("logInValid");

							String productProvider = scanner.nextLine();
							String productName = scanner.nextLine();

							for(Product p : server.products) {
								if(p.getName().equals(productName) && p.getProvider().equals(productProvider) ) {
									// tells Client that the purchase was successful
									printstream.println("purchaseValid");
								}
							}
						}
					}
					catch(Exception e) {
						printstream.println("Error purchasing Product! Please try again.");
					}
				}
			}
			scanner.close();
		}
	}

	class AcceptClients implements Runnable {
		private Server server;
		private ServerSocket serversocket;

		public AcceptClients(Server server, ServerSocket serversocket) {
			this.server = server;
			this.serversocket = serversocket;
		}

		public void run() {
			try {
				while (true) {
					// waits for a Client to connect
					Socket client = this.serversocket.accept();

					// starts Thread to deal with each individual Client
					DealWithClient deal = new DealWithClient(client.getInputStream(), new PrintStream(client.getOutputStream()), this.server);
					new Thread(deal).start();
				}
			}
			catch(Exception e) {
				System.out.println("Error connecting a client!");
			}
		}
	}

	public boolean validClient(int clientID, String clientPassword) {
		for(User u : this.users) {
			if( (clientID == u.getID()) && (clientPassword.equals(u.getPassword())) ) {
				return true;
			}
		}
		return false;
	}

	public void clearConsole() {
		for (int i = 0; i < 150; i++) {
    		System.out.println();
        }
    }

	public void init() throws IOException {
 		// creates Lists from CSV data
		updateFromCSV();
		clearConsole();

		// creates ServerSocket and prints its Port number
		ServerSocket serversocket = new ServerSocket(0);

		// starts Thread that accepts clients 
		AcceptClients accept = new AcceptClients(this, serversocket);
		new Thread(accept).start();

		// shows Server menu
		boolean quit = false;
		String strServerInput = new String();
		while(!quit) {
			try {
				System.out.println("Server open! Port: " + serversocket.getLocalPort());
				System.out.println("***************Server Menu**************");
				System.out.println("*      1 - Register new product        *");
				System.out.println("*      2 - Show all products           *");
				System.out.println("*      3 - Exit                        *");
				System.out.println("****************************************");
				// reads input
				strServerInput = br.readLine();

				// registers new product
				if(strServerInput.equals("1")) {
					clearConsole();
					createProduct();
				}

				// shows all products
				else if(strServerInput.equals("2")) {
					clearConsole();
					showProducts();
				}

				// exit program
				else if(strServerInput.equals("3")) {
					quit = true;
				}
				else {
					System.out.println("Invalid option! Try again.");
					clearConsole();
				}
			}
			catch(IOException e) {
				// clearConsole();
				System.out.println("Invalid input! Try again.");
			}
		}
	}

	public void createProduct() {
		try {
			System.out.println("Enter new product's name:");
			String productName = br.readLine();
			System.out.println("Enter new product's price:");
			String productPrice = br.readLine();
			System.out.println("New product's best before what date?");
			String productBestBefore = br.readLine();
			System.out.println("Enter new product's provider:");
			String productProvider = br.readLine();

			Product newProduct = new Product(productName, productPrice, productBestBefore, productProvider);
            products.add(newProduct);
            clearConsole();
            System.out.println("New Product created!\n");
        }
		catch(Exception e) {}
	}

	public void showProducts() {
		System.out.println("Name | Price | Best Before | Provider");
		System.out.println("-------------------------------------");
		for(Product p : products) {
			System.out.println(p.getName() + " | " + p.getPrice() + " | " + p.getBestBefore() + " | " + p.getProvider());
		}
		System.out.println("-------------------------------------");
	}

	public void updateFromCSV() {
        try {
            // updating Users
            File fileUsers = new File("users.txt");
            // if file doesn't exists, then create it
            if (!fileUsers.exists()) {
				fileUsers.createNewFile();
            }
            BufferedReader usersCSV = new BufferedReader(new FileReader(fileUsers));
            String line = null;
            while ( (line = usersCSV.readLine()) != null) {
            	String[] parts = line.split("\\|");
                String userName = parts[0];
                String userAddress = parts[1];
                String userPhone = parts[2];
                String userEmail = parts[3];
                int userID = Integer.parseInt(parts[4]);
                String userPassword = parts[5];

                User newUser = new User(userName, userAddress, userPhone, userEmail, userID, userPassword);
                users.add(newUser);
            }
        
            // updating Products
            File fileProducts = new File("products.txt");
            // if file doesn't exists, then create it
            if (!fileProducts.exists()) {
                fileProducts.createNewFile();
            }
            BufferedReader productsCSV = new BufferedReader(new FileReader(fileProducts));
            line = null;
            while ( (line = productsCSV.readLine()) != null) {
                String[] parts = line.split("\\|");
                String productName = parts[0];
                String productPrice = parts[1];
                String productBestBefore = parts[2];
                String productProvider = parts[3];

                Product newProduct = new Product(productName, productPrice, productBestBefore, productProvider);
                this.products.add(newProduct);
			}
		}
        catch (Exception e) {}
    }

    public static void updateToCSV() {
    	try {
	    	// updating Users
	        File fileUsers = new File("users.txt");
	        PrintWriter usersCSV = new PrintWriter(fileUsers);
	        for(User u : users) {
	        	usersCSV.println(
	        		u.getName() + "|" +
	        		u.getAddress() + "|" +
	        		u.getPhone() + "|" +
	        		u.getEmail() + "|" +
	        		u.getID() + "|" +
	        		u.getPassword() + "|"
	        	);
	        }
	        usersCSV.close();

	        // updating Products
	        File fileProducts = new File("products.txt");
	        PrintWriter productsCSV = new PrintWriter(fileProducts);
	        for(Product p : products) {
	        	productsCSV.println(
	        		p.getName() + "|" +
	        		p.getPrice() + "|" +
	        		p.getBestBefore() + "|" +
	        		p.getProvider() + "|"
	        	);
	        }
	        productsCSV.close();

	    }
	    catch(Exception e) {
	    	System.out.println("Error on updateToCSV!!!");
	    }
    }
}