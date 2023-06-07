package com.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppTwoSubscriber {
    private static final int MULTICAST_PORT = 8888;
    private static final String MULTICAST_GROUP = "225.0.0.1";
    private static final int APP_TWO_PORT = 9999; // Port number for App Two

    public static void main(String[] args) {
        try {
            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_GROUP);
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.joinGroup(multicastAddress);

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            LocalDateTime stoppingTime = null; // Variable to store the stopping time
            boolean bypassComparison = false; // Flag to bypass the comparison of the seconds part

            // Create a socket to receive notifications from App Three
            DatagramSocket notificationSocket = new DatagramSocket(APP_TWO_PORT);

            while (true) {
                multicastSocket.receive(packet);
                String receivedDataWithPublisherName= new String(packet.getData(), 0, packet.getLength());
                
                 // Splitting received data into publisher name and data
                 String[] parts=receivedDataWithPublisherName.split(" ",2);
                 String publisherName=parts[0];
                 String receivedData=parts[1];

                 // Parse the received timestamp
                 DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                 LocalDateTime publisherTime=LocalDateTime.parse(receivedData.trim(),formatter);

                 // Compare the seconds part of the publisher's time to receiver's time
                 if(bypassComparison || stoppingTime==null || publisherTime.getSecond()!=stoppingTime.getSecond()){
                     System.out.println("Received from publisher ("+publisherName+"): "+receivedData.trim());
                 }else{
                     // Stop printing when seconds match
                     System.out.println("Stopping printing for App Two");

                     // Wait for notification from App Three
                     byte[] notificationBuffer=new byte[256];
                     DatagramPacket notificationPacket=new DatagramPacket(notificationBuffer,notificationBuffer.length);
                     notificationSocket.receive(notificationPacket);
                     String notificationMessage=new String(notificationPacket.getData(),0,notificationPacket.getLength());

                     if("continue".equals(notificationMessage)){
                         System.out.println("Received notification from App Three to continue printing.");
                         bypassComparison=true; // Bypass comparison of seconds part
                     }else{
                         // Close sockets
                         multicastSocket.leaveGroup(multicastAddress);
                         multicastSocket.close();
                         notificationSocket.close();
                         break;
                     }
                 }

                 // Store stopping time if not set
                 if(stoppingTime==null){
                     stoppingTime=publisherTime;
                     System.out.println("Stopping time set for apptwo: "+stoppingTime.format(formatter));
                 }
             }
         }catch(Exception e){
             e.printStackTrace();
         }
     }
}
