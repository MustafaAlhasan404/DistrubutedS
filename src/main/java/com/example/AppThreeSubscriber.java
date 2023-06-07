package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class AppThreeSubscriber {
    private static final String MULTICAST_ADDRESS = "225.0.0.1";
    private static final int PORT = 8888;
    private static final String FIRST_RECEIVER_IP = "127.0.0.1";
    private static final int FIRST_RECEIVER_PORT = 9999;

    private static MulticastSocket multicastSocket;
    private static int count = 0; // Number of times the second number is a multiple of 4
    private static boolean continuePrinting = true;

    public static void main(String[] args) {
        try {
            InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket = new MulticastSocket(PORT);
            multicastSocket.joinGroup(multicastGroup);
            System.out.println("App Three is running...");

            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                String receivedDataWithPublisherName= new String(packet.getData(), 0, packet.getLength());

                // Splitting received data into publisher name and data
                String[] parts=receivedDataWithPublisherName.split(" ",2);
                String publisherName=parts[0];
                String receivedData=parts[1];

                // Extract the second number from the received data
                int secondNumber = MulticastUtils.extractSecondNumber(receivedData);

                // Check if the second number is a multiple of 4
                if (secondNumber % 4 == 0) {
                    count++;
                    if (count == 3) {
                        // Stop printing after the third time the second number is a multiple of 4
                        stopPrinting();
                        if (!continuePrinting) {
                            break;
                        }
                    }
                }

                if (continuePrinting) {
                    printReceivedData(publisherName, receivedData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            multicastSocket.close();
        }
    }

    /**
     * Stops printing and sends a message to the first receiver to continue printing.
     */
    private static void stopPrinting() {
        continuePrinting = false;
        System.out.println("Stopping printing for App Three");

        // Prompt the user for input
        Scanner scanner = new Scanner(System.in);
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(FIRST_RECEIVER_IP);
            byte[] buffer = "continue".getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, FIRST_RECEIVER_PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Do you want to continue receiving messages from the publisher? (y/n)");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("y")) {
            continuePrinting = true;
            System.out.println("Continuing to receive messages from the publisher.");
        }

        // Send a message to the first receiver to continue printing
    }

    /**
     * Prints the received data.
     *
     * @param publisherName The name of the publisher.
     * @param data The received data.
     */
    private static void printReceivedData(String publisherName, String data) {
        System.out.println("Received from publisher (" + publisherName + "): " + data);
    }
}
