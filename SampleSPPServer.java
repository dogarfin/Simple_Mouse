import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
 
import javax.bluetooth.*;
import javax.microedition.io.*;
 
/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class SampleSPPServer {
   
    //start server
    private void startServer() throws IOException{
 
        //Create a UUID for SPP
        UUID uuid = new UUID(0x1101);
        //Create the servicve url
        String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";
       
        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );
       
        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection=streamConnNotifier.acceptAndOpen();
 
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));
       
        //read string from spp client
        InputStream inStream=connection.openInputStream();
        
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()
        
        float speedx = 0;
        float speedy = 0;

        boolean first = true;
        boolean xMode = true;
        boolean yMode = false;
        String str = "";
        // Keep listening to the InputStream until an exception occurs
        while (true) {
        	if (!first) {
            try {
            	Robot r = new Robot();
            	//ByteArrayInputStream input = new ByteArrayInputStream(buffer);
            	//System.out.println(input);
                // Read from the InputStream
                bytes = inStream.read(buffer);
                // Send the obtained bytes to the UI activity
                
                if(xMode) {
                	if((char) buffer[0] ==  "X".charAt(0)) {
                		Point location = MouseInfo.getPointerInfo().getLocation();
                		System.out.println(location);
                		
                		int x = (int) location.getX();
                		
                		int y = (int) location.getY();
                		
                		Float data = Float.valueOf(str);
                		System.out.println(data);
                		
                		float abs = Math.abs(data);
                		
                		if (abs < 1) {
                			data = (float) 0.0;
                		}
                		if (data > 10) {
                			data = (float) 10.0;
                		}
                		if (data < -10) {
                			data = (float) -10.0;
                		}
                		
                		speedx = speedx + data;
                		if (speedx < 1) 
                			speedx = 0;
                		
                		if (speedx >= 10) 
                			speedx = 10;
                		
                		if (speedx <= -10)
                			speedx = -10;
                			
                		
                		System.out.println(speedx);
                		
                		int newx = x + Math.round(1 * speedx);
                		
                		r.mouseMove(newx, y);
                		str = "";
                		xMode = false;
                		yMode = true;
                } else {
                	if ((char) buffer[0] !="Y".charAt(0))
                	str = str + (char) buffer[0];
                }
                }
                
                if(yMode) {
                	if((char) buffer[0] ==  "Y".charAt(0)) {
                		Point location = MouseInfo.getPointerInfo().getLocation();
                		System.out.println(location);
                		
                		int x = (int) location.getX();
                		
                		int y = (int) location.getY();
                		
                		Float data = Float.valueOf(str);
                		System.out.println(data);
                		
                		float abs = Math.abs(data);
                		if (abs < 1) {
                			data = (float) 0.0;
                		}
                		if (data > 10) {
                			data = (float) 10.0;
                		}
                		if (data < -10) {
                			data = (float) -10.0;
                		}
                		
                		speedy = speedy + data;
                		if (speedy < 1) 
                			speedy = 0;
                		
                		if (speedy >= 10) 
                			speedy = 10;
                		
                		if (speedy <= -10)
                			speedy = -10;
                		
                		System.out.println(speedy);
                		
                		int newy = y + Math.round(1 * speedy);
                		
                		r.mouseMove(x, newy);
                		str = "";
                		xMode = true;
                		yMode = false;
                } else {
                	if ((char) buffer[0] !="X".charAt(0))
                	str = str + (char) buffer[0];
                }
                	
                }
                     
            } catch (IOException | AWTException e) {
                break;
            }
        	}
            if (first) first = false;
        }
        /*
    	if (str == "LC"){
        	r.mousePress(InputEvent.BUTTON1_MASK);
        	r.mouseRelease(InputEvent.BUTTON1_MASK);
        }
    	if (str == "RC"){
            r.mousePress(InputEvent.BUTTON3_MASK);
            r.mouseRelease(InputEvent.BUTTON3_MASK);
        }
        */
        streamConnNotifier.close();
 
    }
 
 
    public static void main(String[] args) throws IOException {
       
        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());
       
        SampleSPPServer sampleSPPServer=new SampleSPPServer();
        sampleSPPServer.startServer();
       
    }
}