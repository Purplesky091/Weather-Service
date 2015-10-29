import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import java.net.*;

import javax.imageio.ImageIO;

public class Client {

	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	
	//receives an image from the server and puts the image into the specified file.
	private void receiveImage(String fileName)
	{
		DataInputStream dis;
		try
		{
			dis = new DataInputStream(socket.getInputStream());
			int len = dis.readInt();//receive how many bytes the image is.			
			byte[] data = new byte[len];
			dis.readFully(data);//put the image bytes into byte array.
			
			InputStream ian = new ByteArrayInputStream(data);//put the bytes into a byte array input stream
			BufferedImage image = ImageIO.read(ian);//read bytes in as image.
			
			System.out.println();
			System.out.println(fileName + " saved.");
			File cityPic = new File(fileName);
			ImageIO.write(image, "JPG", cityPic);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void communicate(String h) {
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter your name: ");
		String name = scan.nextLine();
		out.println(name);
		
		//variables needed to communicate
		int locationNum = -1;
		boolean exit = false;
		boolean isInvalidInput = false;
		String choice;
		String line;
		String menu = "\nChoose from the following list of commands:\nA.   Select a location.\nB.   Get temperature at selected location.\nC.   Get wind direction and speed at selected location.\nD.   Get image from selected location.\nE.   Exit.\nEnter choice: ";
		
		while (!exit) {
			do {
				System.out.print(menu);
				choice = scan.nextLine();
				line = null;
				choice = choice.toUpperCase();
				
				//loop to keep getting input from user until they enter something valid
				switch(choice) {
					case "A":
					case "B":
					case "C":
					case "D":
					case "E":
						isInvalidInput = false;
						break;//if user inputs A - E, then it's a valid input
						
					default:
						isInvalidInput = true;
						break;
				}
			} while (isInvalidInput);
			out.println(choice);
			try { //user choice input to determines how many lines to read and what 
				switch(choice) {
					case "A": //read as many lines as indicated by integer received first in input stream then output them
						line = in.readLine();
						int locations = Integer.valueOf(line);
						System.out.println();
						for (int i = 0; i < locations; i++) {
							line = in.readLine();
							System.out.println(line);
						}
						System.out.print("Enter choice: ");
						String loc = scan.nextLine(); //prompt user to select location then sends selection integer to server and sets location in client global variables
						locationNum = Integer.valueOf(loc);
						out.println(loc);
						break;
					case "B":
						line = in.readLine();
						System.out.println("");
						System.out.println(line);
						break;
					case "C":
						line = in.readLine();
						System.out.println("");
						System.out.println(line);
						break;
					case "D":
						String fileName = null;
						switch (locationNum) { //sets filename depending on user choice for location
							case -1://if location wasn't selected
								line = in.readLine();
								System.out.println("");
								System.out.println(line);
								continue;//jump back to the beginning of the while(!exit) loop. This brings the user back to the menu screen.
						
							case 1:
								fileName = "Albuquerque.jpg";
								break;
							case 2:
								fileName = "Dallas.jpg";
								break;
							case 3:
								fileName = "Tampa.jpg";
								break;
							default:
								fileName = "nothing.jpg";
								break;
						}
						
						receiveImage(fileName);	// receives image from server					
						break;
					case "E": //close scanner and socket then exits from while loop
						scan.close();
						socket.close();
						System.out.println("\nDisconnected from "+h+".");
						exit = true;
						break;
				}
			}
			catch (IOException e) {
				System.out.println("Read failed");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void listen(String host, int port) {
		try { //creates socket, printwriter, and bufferedreader objects for client and connects to server
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (UnknownHostException e) {
			System.out.println("Unknown Host");
			System.exit(1);
		} 
		catch (IOException e) {
			System.out.println("No I/O");
			System.exit(1);
		}

	}
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Client hostname port");
			System.exit(1);
		}

		Client client = new Client();

		String host = args[0];
		int port = Integer.valueOf(args[1]);
		client.listen(host, port);
		System.out.println("Client connected on "+host+" port "+port);
		client.communicate(host);
	}
}