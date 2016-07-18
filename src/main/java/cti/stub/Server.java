package cti.stub;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by srg on 13.07.16.
 */
public class Server {
    public static final int PORT = 42027;
    private Logger logger = Logger.getLogger(Server.class.getClass().getName());
    private ExecutorService service;


    //Methods
    public static void main(String[] args) {
        Server s = new Server();
        s.establishThreadPool(3);
        if (args.length > 0 && args[0] != null && !args[0].isEmpty())
            s.loadEstablishConnectionScenarioFile(args[0].toString());
        else s.loadEstablishConnectionScenarioFile("/home/user/tmp/scenarios_short1.xml");
    }

    /**
     * Establish connection with client
     */
    private void connect() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        Selector selector = Selector.open();
        logger.log(Level.FINE, "Waiting for connection on port ".concat(String.valueOf(PORT)));

        try {
            ssc.configureBlocking(true);
            ssc.socket().bind(new InetSocketAddress(PORT));
            SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);


            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    iterator.remove();
                    if(selectionKey.isAcceptable()){
                        SocketChannel clientChannel = ssc.accept();
                    }
                }
            }


        } finally {
            ssc.close();
            selector.close();
        }

    }

    private void establishThreadPool(int threadCount) {
        service = Executors.newFixedThreadPool(threadCount);
    }


    private void loadEstablishConnectionScenarioFile(String scenarioFilePath) {
        try {
            long initTime = System.currentTimeMillis();
            logger.log(Level.INFO, String.format("Loading scenarioInitiateConnection from file: %s", scenarioFilePath));
            Map<String, Object> tmp = ClientDescriptor.parseScenarioContainer(scenarioFilePath);
            tmp = ClientDescriptor.preCompile(tmp);
            this.scenarioInitiateConnection = tmp;
            logger.log(Level.INFO, String.format("Script preparing time: %s ms", System.currentTimeMillis() - initTime));
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }

    }

    private void doLabel() {
        System.out.println("*****   *******     **   **     **   **     *****       *****   ******  *******     *******");
        System.out.println("*****   *******     **   **     **   **     *****       *****   ******  *******     *******");
        System.out.println("**      **   **     ***  **     ***  **     **          **        **    **   **     **   **");
        System.out.println("**      **   **     ** * **     ** * **     *****       **        **    **   **     *******");
        System.out.println("**      **   **     **  ***     **  ***     **          **        **    **   **     **  ** ");
        System.out.println("*****   *******     **   **     **   **     *****       *****     **    *******     **   **");
        System.out.println("*****   *******     **   **     **   **     *****       *****     **    *******     **   **");

    }
}
