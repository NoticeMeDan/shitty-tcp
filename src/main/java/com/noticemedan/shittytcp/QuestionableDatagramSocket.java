package com.noticemedan.shittytcp;

public class QuestionableDatagramSocket {
    public static void main(String[] args) {
        System.out.println("God i'm questionable");


    }

    public void executeRandomDatagram(){
        int random = (int) (Math.random() * 4);

        switch (random){
            case(0): // Discard

                break;
            case(1): // Create duplicates

                break;
            case(2): // Simply send the datagram

                break;
            case(3): // Reorders the datagram

                break;
        }
    }
}
