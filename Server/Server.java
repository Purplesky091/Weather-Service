import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Server
{
	//constants representing the different cities
	static final int ALBUQUERQUE = 0;
	static final int DALLAS = 1;
	static final int TAMPA = 2;
	
	ServerSocket server = null;
	String locations[] = {"Albuquerque, NM", "Dallas, TX", "Tampa, FL"};
	Map<Integer, String> cityTempMap = new HashMap< >();//holds all the temperatures of the cities. Input the city and the map gives back the temperature
	Map<Integer, String> cityWindMap = new HashMap< >();
	Map<Integer, File> cityPictureMap = new HashMap< >();
	
	public Server()
	{
		//initialize the cityTempMap
		cityTempMap.put(ALBUQUERQUE, "42");
		cityTempMap.put(DALLAS, "62");
		cityTempMap.put(TAMPA, "68");
		
		cityWindMap.put(ALBUQUERQUE, "SouthEast, 5");
		cityWindMap.put(DALLAS, "NorthWest, 10");
		cityWindMap.put(TAMPA, "SouthWest, 7");
		
		cityPictureMap.put(ALBUQUERQUE, new File("Albuquerque.jpg"));		
		cityPictureMap.put(DALLAS, new File("Dallas.jpg"));
		cityPictureMap.put(TAMPA, new File("Tampa.jpg"));
	}
	
	public void listen(int port)
	{
		try
		{
			server = new ServerSocket(port);
			
			while(true)
			{
				//assign a client to a clientworker and then launch the clientworker as a thread.
				ClientWorker w = new ClientWorker(server.accept());
				Thread t = new Thread(w);
				t.start();
			}			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.err.println("Format: java Server port");
			System.exit(1);
		}
		
		int port = Integer.valueOf(args[0]);
		Server server = new Server();
		
		System.out.println("Server running on port " + port +
				", " + " use ctrl-c to end");
		server.listen(port);
	}
	
	//ClientWorker inner class comes after Server's code
	class ClientWorker implements Runnable
	{
		Socket client = null;
		BufferedReader in = null;
		PrintWriter out = null;
		String clientName = "";//client sends in their name
			
		int citySelected = -1;//this represents the city the client currently selected

		ClientWorker(Socket c)
		{
			client = c;
		}
		
		private void sendLocations()
		{
			String locationNum = Integer.toString(locations.length);
			out.println(locationNum);
			
			int i = 1;//the number in front of the location
			System.out.println("Sending location list to " + clientName);
			for(String location: locations)
			{
				location = i + ". " + location;
				out.println(location);
				i++;
			}
		}
		
		//gets the location the client selects.
		//saves selected city into citySelected variable
		private void receiveLocation() throws IOException
		{
			String clientCity = in.readLine();
			citySelected = Integer.valueOf(clientCity) - 1;//# client sends = the city's index + 1. We're going to subtract 1 to get the city's index
			System.out.println(clientName + " selected " + locations[citySelected]);
		}
		
		private void sendTemperature()
		{
			if(citySelected == -1)
			{
				String errorMessage = "ERROR: client did not select a city";
				System.err.println(errorMessage);
				out.println(errorMessage);//sends an error message to the client if the client hadn't selected a city yet.
				return;
			}
			
			System.out.println("Sending " + locations[citySelected] + " temperature to " + clientName);
			String line = "Temperature in " + locations[citySelected] + ": " + cityTempMap.get(citySelected) + " F";
			out.println(line);
		}
		
		private void sendWind()
		{
			if(citySelected == -1)
			{
				String errorMessage = "ERROR: client did not select a city";
				System.err.println(errorMessage);
				out.println(errorMessage);
				return;
			}
			
			System.out.println("Sending " + locations[citySelected] + " wind reading to " + clientName);
			String line = "Wind reading in " + locations[citySelected] + ": " + cityWindMap.get(citySelected) + " mph";
			out.println(line);
		}
		
		private void sendImage() throws IOException
		{			
			if(citySelected == -1)
			{
				String errorMessage = "ERROR: client did not select a city";
				System.err.println(errorMessage);
				out.println(errorMessage);
				return;
			}
			
			BufferedImage image = ImageIO.read(cityPictureMap.get(citySelected));
			
			//convert the image into bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "JPG", baos);
			baos.flush();
			baos.close();//closing the byte array output stream so that imageIO will finish writing the rest of the image
			byte [] bytes = baos.toByteArray();
						
			//send the bytes to the client
			DataOutputStream daos = new DataOutputStream(client.getOutputStream());
			daos.writeInt(bytes.length);//tells the client how big the image is in advance
			daos.write(bytes, 0, bytes.length);
			System.out.println("Sending " + locations[citySelected] + " current image to " + clientName);
		}
		
		public void run()
		{
			String clientLetter;//Initialized in the client server interaction
			boolean keepGoing = true;

			try
			{
				//initializing input and output streams
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);
				
				//initialize client name here
				clientName = in.readLine();
				System.out.println(clientName + " connected");//even though client already connected in constructor, 
			}
			catch (IOException e)
			{
				System.err.println("I/O failed");
				System.exit(-1);
			}			
			
			//interaction with the client starts here
			while(keepGoing)
			{
				try
				{
					clientLetter = in.readLine();//read in client's input
					switch(clientLetter)
					{
						case "A":
							sendLocations();
							receiveLocation();
							break;
							
						case "B":
							sendTemperature();
							break;
							
						case "C":
							sendWind();
							break;
							
						case "D":
							sendImage();
							break;
							
						case "E":
							keepGoing = false;
							System.out.println(clientName + " disconnected");
							break;
					}		
				}//Part underneath is just exception handling and closing the client's socket
				catch (IOException e)
				{
					System.out.println("Read failed");
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			try
			{
				client.close();
			}
			catch (IOException e)
			{
				System.out.println("Close failed");
				System.exit(-1);
			}
		}
	}
}
