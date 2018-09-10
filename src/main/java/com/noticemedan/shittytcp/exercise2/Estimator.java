package com.noticemedan.shittytcp.exercise2;

import com.noticemedan.shittytcp.exercise1.QuestionableDatagramSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;


public class Estimator {

    private int numOfLost,
                numOfDuplicated,
                numOfReordered;
    private int ammount;

    private QuestionableDatagramSocket socket;

    public Estimator(int datagramSize, int numOfDatagrams, int transInterval){
        if(datagramSize > 60.000){
            throw new IllegalArgumentException("Please provide a datagram size less than 60.000");
        }
        this.ammount = numOfDatagrams;
        try {
            this.sendRandomDatagrams(datagramSize, numOfDatagrams, transInterval);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Main method for sending random datagrams
    private void sendRandomDatagrams(int size, int num, int interval) throws IOException, InterruptedException {
        //The host of our RFC862 server
        InetAddress aHost = InetAddress.getByName("localhost");
        //The last-sent packet stored and used to identify reordered packets
        DatagramPacket prevPacket;

        //Making sure we can read System.out from our RFC862 server;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(pipeOut));

        //Runs the loop #num amount of times, sending and receiving answers, recording drops, dupes and reorders.
        for(int i = 0; i<num; i++) {
            //Create random packet as 'request'
            QuestionableDatagramSocket aSocket = new QuestionableDatagramSocket();
            byte[] m = generateRandomByteArray(size);
            DatagramPacket request = new DatagramPacket(m, num, aHost, 7007);
            prevPacket = request;
            //send request
            aSocket.send(request);

            //Checks what our console has written
            String output = new String(pipeOut.toByteArray());
            //This is the expected string RFC862 Sends if it is succesfully sent
            String expectedResult = "RECIEVED: " + Arrays.toString(request.getData());

            if(output.equals(expectedResult)){
                //All is fine, do nothing.
            }
            //TODO: Check whether or not this actually checks for duplicate messages
            else if(output.equals(expectedResult + expectedResult)){
                this.numOfDuplicated++;
            }//TODO: Check whether or not this actually checks for previous messages
            else if(output.equals("RECIEVED: " + Arrays.toString(prevPacket.getData()))){
                this.numOfReordered++;
            }
            else {
                //Otherwise we've dropped the message, probably.
                this.numOfLost++;
            }
            //Close the socket, and start again after #interval amount of time.
            aSocket.close();
            Thread.sleep(interval);
        }

        //Remember to set the System.Out back to where it was!
        System.setOut(oldOut);
        System.out.println("All done!");

    }

    private byte[] generateRandomByteArray(int s){
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s; i++) {
            char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString().getBytes();
    }

    public int getNumOfLost() {
        return numOfLost;
    }

    public int getNumOfDuplicated() {
        return numOfDuplicated;
    }

    public int getNumOfReordered() {
        return numOfReordered;
    }

    public String getPercOfLost() {
        return ammount/numOfLost + "%";
    }

    public String getPercOfDuplicated() {
        return ammount/numOfDuplicated + "%";
    }
}
