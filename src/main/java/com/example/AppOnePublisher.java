package com.example;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppOnePublisher {
    private static final int MULTICAST_PORT = 8888;
    private static final String MULTICAST_GROUP = "225.0.0.1";
    private static final int BUFFER_SIZE = 256;

    public static void main(String[] args) {
        try {
            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_GROUP);
            MulticastSocket socket = new MulticastSocket();
            socket.joinGroup(multicastAddress);

            boolean stopped = false;

            while (!stopped) {
                // Get the current timestamp
                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                String message = "AppOne " + timestamp.format(formatter);

                // Create a datagram packet and send it to the multicast group
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAddress, MULTICAST_PORT);
                socket.send(packet);

                System.out.println("Published From AppOnePublisher : " + message);

                // Check if a "STOP" message has been received
                stopped = MulticastUtils.checkForStopMessage(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
 class MulticastUtils {
    private static final int BUFFER_SIZE = 256;

    public static boolean checkForStopMessage(MulticastSocket socket) throws SocketException {
        boolean stopped = false;
        socket.setSoTimeout(1000);
        try {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String receiveMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (receiveMessage.equals("STOP")) {
                stopped = true;
                System.out.println("Received STOP message. Stop publishing from AppOnePublisher.");
            }
        } catch (Exception e) {
            // Ignore any exceptions caused by not receiving a packet within the timeout period
        }
        return stopped;
    }

    public static void sendStopMessage(MulticastSocket socket, InetAddress address, int port) throws IOException {
        String stopMessage = "STOP";
        byte[] stopBuffer = stopMessage.getBytes();
        DatagramPacket stopPacket = new DatagramPacket(stopBuffer, stopBuffer.length, address, port);
        socket.send(stopPacket);
    }

    public static int extractSecondNumber(String data) {
        String[] parts = data.split(" ");
        String time = parts[1];
        String[] timeParts = time.split(":");
        String seconds = timeParts[2].split("\\.")[0];
        return Integer.parseInt(seconds);
    }
}