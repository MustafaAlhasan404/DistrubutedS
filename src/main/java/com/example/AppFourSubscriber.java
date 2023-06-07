package com.example;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppFourSubscriber {
    private static final int MULTICAST_PORT = 8888;
    private static final String MULTICAST_GROUP = "225.0.0.1";
    private static final int BUFFER_SIZE = 256;

    public static void main(String[] args) {
        try {
            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_GROUP);
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(multicastAddress);

            long startTime = 0;
            boolean publisherStopped = false;
            boolean firstPacketReceived = false;

            while (true) {
                // Receive data from the multicast group
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                if (!firstPacketReceived) {
                    startTime = System.currentTimeMillis();
                    firstPacketReceived = true;
                }

                String message = new String(packet.getData(), 0, packet.getLength());
                if (!publisherStopped) {
                    System.out.println("Received from AppOnePublisher: " + message);
                }

                // Check if 30 seconds have passed
                if (System.currentTimeMillis() - startTime >= 30000 && !publisherStopped) {
                    // Tell the publisher to stop publishing
                    MulticastUtils.sendStopMessage(socket, packet.getAddress(), packet.getPort());

                    publisherStopped = true;
                }

                // If the publisher has been stopped, start publishing the current time
                if (publisherStopped) {
                    publishCurrentTime(socket, multicastAddress);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishes the current time to the multicast group.
     *
     * @param socket The socket to use for sending data.
     * @param multicastAddress The address of the multicast group.
     */
    private static void publishCurrentTime(MulticastSocket socket, InetAddress multicastAddress) throws Exception {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String publishMessage = "AppFour " + timestamp.format(formatter);

        byte[] publishBuffer = publishMessage.getBytes();
        DatagramPacket publishPacket = new DatagramPacket(publishBuffer, publishBuffer.length, multicastAddress, MULTICAST_PORT);
        socket.send(publishPacket);

        System.out.println("Published from AppFourReciver: " + publishMessage);
    }
}
