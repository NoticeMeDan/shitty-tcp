package com.noticemedan.shittytcp.exercise2;

import com.noticemedan.shittytcp.exercise1.QuestionableDatagramSocket;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Estimator {

    private int datagramSize, numOfDatagrams;

    private ArrayList<DatagramPacket> send;
    private LinkedList<DatagramPacket> sendList;
    private ArrayList<DatagramPacket> received;

    private QuestionableDatagramSocket sendSocket;
    private DatagramSocket receiveSocket;

    // Count actions
	int currentLength = 0;
	int duplicatedSocket = 0; int reorderedSocket = 0; int droppedSocket = 0; int receivedSocket = 0;

    public Estimator(int datagramSize, int numOfDatagrams){
        this.datagramSize = datagramSize > 60000 ? 60000 : datagramSize;
        this.numOfDatagrams = numOfDatagrams;
        this.received = new ArrayList<>();
        try {
            this.sendSocket = new QuestionableDatagramSocket();
            this.receiveSocket = new DatagramSocket(7007);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public static void estimate(int datagramSize, int numOfDatagrams, int interval) {
        Estimator est = new Estimator(datagramSize, numOfDatagrams);
        est.createRandomDatagrams();

        Runnable sender = () -> {
            try {
                est.sendSocket.send(est.sendList.pop());
				est.updateCount();
            } catch (Exception e) {
                System.out.println("No more elements in list!");
            }
        };

        Runnable receiver = () -> {
            while(true) {
                try {
                    byte[] buffer = new byte[est.datagramSize];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    est.receiveSocket.receive(reply);
                    est.received.add(reply);

                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout. Closing...");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        //Open the receiver thread and start listening for Packets on the DatagramSocket.
        Thread receiveThread = new Thread(receiver);
        receiveThread.start();

        //Start a Thread with an Executor Service that runs the packet-sending method with the given interval
        ScheduledExecutorService sendScheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture handler = sendScheduler.scheduleAtFixedRate(sender, interval, interval, TimeUnit.MILLISECONDS);
        sendScheduler.schedule(() -> {
        	handler.cancel(true);
        	est.printResults();
		   try {
		   		receiveThread.join();   //Close the receive thread.
		   } catch (InterruptedException e) { e.printStackTrace(); }

	   	},interval * est.send.size(), TimeUnit.MILLISECONDS);
    }

	private void updateCount() {
    	int newCurrentSize = this.received.size();

    	if (newCurrentSize  == this.currentLength) {
			this.droppedSocket++;
		} else if ((newCurrentSize  - this.currentLength) == 1 ){
    		this.receivedSocket++;
		} else if (((newCurrentSize  - this.currentLength)) == 2){
    		this.duplicatedSocket++;
		}

    	this.currentLength = newCurrentSize;
	}

	private void printResults() {
    	/*
		for(int i = 0; i<send.size(); i++) {
			int num = Collections.frequency(received, send.get(i));
			if (num == 1){
				//Check for reorder or sent
				sent++;
			}
			else if(num == 2){
				duplicated++;
			}
			else{
				dropped++;
			}
		}

    	byte[] prevSent = new byte[0];
    	byte[] prevRec  = new byte[1];
    	byte[] currSent; byte[] currRec;



		for(int i = 0; i<send.size(); i++){
        	currSent = send.get(i).getData();
        	currRec = received.get(i).getData();

			if(Arrays.equals(currSent, currRec)){
				sent++;
			}
			else if(Arrays.equals(currRec, prevRec)){
				duplicated++;
				sent--;
			}
			else if(Arrays.equals(currSent, prevRec) && Arrays.equals(prevSent, currRec)){
				reordered++;
			}
			else {
				if(i != 0)dropped++;
			}
			prevSent = currSent;
			prevRec = currRec;
		}
		*/


		System.out.println("######## STATISTICS ########");
        System.out.println("Duplicates: " + this.duplicatedSocket);
        System.out.println("Reordered: " + this.reorderedSocket);
        System.out.println("Discarded: " + this.droppedSocket);
		System.out.println("Received: " + this.receivedSocket);
        System.out.println("Packets sent: " + this.send.size());
        System.out.println("Packets received: " + this.received.size());
    }

    private void createRandomDatagrams() {
        InetAddress aHost = null;
        try {
            aHost = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.send = new ArrayList<>();
        this.sendList = new LinkedList<>();

        if (this.datagramSize > 60000){
            throw new IllegalArgumentException("Please provide a datagram size less than 60.000");
        }
        for (int i = 0; i < this.numOfDatagrams; i++) {
            byte[] m = generateRandomByteArray(this.datagramSize);
            DatagramPacket packet = new DatagramPacket(m, this.numOfDatagrams, aHost, 7007);
            this.send.add(packet);
            this.sendList.add(packet);
        }
    }

    private byte[] generateRandomByteArray(int s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s; i++) {
            char c = (char)((int) ThreadLocalRandom.current().nextInt(50, 110 + 1));
            sb.append(c);
        }
        return sb.toString().getBytes();
    }

    public static void main(String args[]) {
        estimate(20,20,100);
    }

}
