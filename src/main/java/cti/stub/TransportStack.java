package cti.stub;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 14.07.16.
 */
public class TransportStack extends Thread {
    static int readCount = 0;
    static int writeCount = 0;
    private static Logger logger = Logger.getLogger(TransportStack.class.getClass().getName());
    private final byte[] HEART_BEAT_REQUEST = ClientDescriptor.hexStringToByteArray("000000040000000500000001");
    private final byte[] HEART_BEAT_RESPONSE = ClientDescriptor.hexStringToByteArray("000000040000000600000001");
    private Queue<byte[]> inputMessages = new ConcurrentLinkedQueue<byte[]>();
    private Queue<byte[]> outputMessages = new ConcurrentLinkedQueue<byte[]>();
    private Socket clientSocket;


    //Constructors
    public TransportStack(Socket s) {
        this.clientSocket = s;
//        logger.setLevel(Level.INFO);
    }

    //Methods
    public static byte[] read(Socket s, boolean shouldWaiting) throws IOException {
        InputStream fromClient = s.getInputStream();
//        System.out.println("available " + fromClient.available());

        if (!shouldWaiting && !(fromClient.available() > 0)) {
//            logger.log(Level.INFO, "nothing to read");
            return null;
        }

        long messageLength = 0L;
        long messageType = 0L;
        int b;
        byte[] messageLengthInByte = new byte[4];
        byte[] messageTypeInByte = new byte[4];
        // определение длинны сообщения

        int counter = 0;
        while (counter < messageLengthInByte.length) {
            b = fromClient.read();
            messageLengthInByte[counter] = (byte) b;
//            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        messageLength = convertByteArraySize4ToLong(messageLengthInByte);
        //надо ли проверять размер сообщения?
        if (messageLength <= 4329) {
//            logger.log(Level.INFO, String.format("message lengths %s - correct", messageLength));
        } else logger.log(Level.INFO, String.format("message lengths %s - incorrect", messageLength));
        // определение типа сообщения
        counter = 0;
        while (counter < messageTypeInByte.length) {
            b = fromClient.read();
            messageTypeInByte[counter] = (byte) b;
//            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        messageType = convertByteArraySize4ToLong(messageTypeInByte);
//        logger.log(Level.INFO, String.format("message type %s", messageType));
        //формирование сообщения
        int offset = messageLengthInByte.length + messageTypeInByte.length;
        byte[] resultMessage = new byte[(int) messageLength + offset];
        System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
        System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
        //сдвиг, учитывающие начальные сообщения
        counter = 0;
        while (counter < messageLength) {
            b = fromClient.read();
            resultMessage[counter + offset] = (byte) b;
//            System.out.print(String.format("%02x", b & 0xFF));
            counter++;
        }
        s.setSoLinger(true, 0);

        return resultMessage;
    }

    public static void write(Socket s, byte[] message) throws IOException {
        OutputStream toClient = s.getOutputStream();
        toClient.write(message);
        toClient.flush();
        s.setSoLinger(true, 0);

//        logger.log(Level.INFO, "MESSAGE SENT");
    }

    private static long convertByteArraySize4ToLong(byte[] variable) {
        long value = 0;
        for (int i = 0; i < variable.length; i++) {
            value = (value << 4) + (variable[i] & 0xff);
        }
        return value;
    }

    //Getters and setters
    public Queue<byte[]> getInputMessages() {
        return inputMessages;
    }

    public void setInputMessages(Queue<byte[]> inputMessages) {
        this.inputMessages = inputMessages;
    }

    public Queue<byte[]> getOutputMessages() {
        return outputMessages;
    }

    public void setOutputMessages(Queue<byte[]> outputMessages) {
        this.outputMessages = outputMessages;
    }

    public int getReadCount() {
        return readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Start transport stack thread");
        super.run();
        byte[] inputMessage;
        byte[] outputMessage;
        while (!isInterrupted()) {
            try {
                inputMessage = read(clientSocket, false);

                if (inputMessage != null && Arrays.equals(inputMessage, HEART_BEAT_REQUEST)) {
                    logger.log(Level.INFO, String.format("GOT HEART_BEAT_REQUEST"));
                    try {
                        write(clientSocket, HEART_BEAT_RESPONSE);
                        inputMessage = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logger.log(Level.INFO, String.format("SENT HEART_BEAT_RESPONSE"));
                }

                if (inputMessage != null) {
                    inputMessages.add(inputMessage);
                    readCount++;
                    logger.log(Level.INFO, String.format("READ MESSAGE FROM NET: " + Hex.encodeHexString(inputMessage)));
                }

                outputMessage = outputMessages.poll();
                if (outputMessage != null) {
                    write(clientSocket, outputMessage);
                    logger.log(Level.INFO, String.format("WROTE MESSAGE TO NET: " + Hex.encodeHexString(outputMessage)));
                    writeCount++;
//                    try {
//                        Thread.currentThread().sleep(500);
//                    } catch (InterruptedException e) {
//                        return;
//                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
