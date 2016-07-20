package cti.stub.exceptions;

import cti.stub.ClientDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * Created by srg on 20.07.16.
 */
public class AsyncTransport implements Runnable {

    private static Logger logger = Logger.getLogger(AsyncTransport.class.getName());
    private final byte[] HEART_BEAT_REQUEST = ClientDescriptor.hexStringToByteArray("000000040000000500000001");
    private final byte[] HEART_BEAT_RESPONSE = ClientDescriptor.hexStringToByteArray("000000040000000600000001");
    private Queue<byte[]> inputMessages = new ConcurrentLinkedQueue<byte[]>();
    private Queue<byte[]> outputMessages = new ConcurrentLinkedQueue<byte[]>();
    private SocketChannel channel;
    private Selector selector;


    //Constructors
    public AsyncTransport() {
    }

    public AsyncTransport(SocketChannel channel) {
        try {
            this.channel = channel;
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Getters and Setters
    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public Queue<byte[]> getOutputMessages() {
        return outputMessages;
    }

    public void setOutputMessages(Queue<byte[]> outputMessages) {
        this.outputMessages = outputMessages;
    }

    public Queue<byte[]> getInputMessages() {
        return inputMessages;
    }

    public void setInputMessages(Queue<byte[]> inputMessages) {
        this.inputMessages = inputMessages;
    }

    //Methods
    @Override
    public void run() {
        boolean done = false;
        try {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            while (!done) {
                selector.select();
                Iterator iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        read();
                    }
                }
            }


        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private byte[] read() {
        ByteBuffer buffer = ByteBuffer.allocate(4329);
        try {
            if (channel.read(buffer) > 0)
                System.out.println(Arrays.toString(buffer.array()));

        } catch (IOException e) {
            e.printStackTrace();
        }

//
//        long messageLength = 0L;
//        long messageType = 0L;
//        int b;
//        byte[] messageLengthInByte = new byte[4];
//        byte[] messageTypeInByte = new byte[4];
//        // определение длинны сообщения
//
//        int counter = 0;
//        while (counter < messageLengthInByte.length) {
//            b = fromClient.read();
//            messageLengthInByte[counter] = (byte) b;
////            System.out.print(String.format("%02x", b & 0xFF));
//            counter++;
//        }
//        messageLength = convertByteArraySize4ToLong(messageLengthInByte);
//        //надо ли проверять размер сообщения?
//        if (messageLength <= 4329) {
////            logger.log(Level.INFO, String.format("message lengths %s - correct", messageLength));
//        } else logger.log(Level.INFO, String.format("message lengths %s - incorrect", messageLength));
//        // определение типа сообщения
//        counter = 0;
//        while (counter < messageTypeInByte.length) {
//            b = fromClient.read();
//            messageTypeInByte[counter] = (byte) b;
////            System.out.print(String.format("%02x", b & 0xFF));
//            counter++;
//        }
//        messageType = convertByteArraySize4ToLong(messageTypeInByte);
////        logger.log(Level.INFO, String.format("message type %s", messageType));
//        //формирование сообщения
//        int offset = messageLengthInByte.length + messageTypeInByte.length;
//        byte[] resultMessage = new byte[(int) messageLength + offset];
//        System.arraycopy(messageLengthInByte, 0, resultMessage, 0, messageLengthInByte.length);
//        System.arraycopy(messageTypeInByte, 0, resultMessage, messageLengthInByte.length, messageTypeInByte.length);
//        //сдвиг, учитывающие начальные сообщения
//        counter = 0;
//        while (counter < messageLength) {
//            b = fromClient.read();
//            resultMessage[counter + offset] = (byte) b;
////            System.out.print(String.format("%02x", b & 0xFF));
//            counter++;
//        }
//        s.setSoLinger(true, 0);

        return null;
    }


}
