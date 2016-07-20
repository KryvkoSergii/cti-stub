package cti.stub;

import cti.stub.exceptions.AsyncTransport;
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
import java.util.concurrent.ConcurrentHashMap;
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
    private Map<String, Object> scenarioInitiateConnection;
    private Map<String, ClientDescriptor> clients = new ConcurrentHashMap<String, ClientDescriptor>();
    private AsyncTransport stack;

    //Methods
    public static void main(String[] args) {
        Server s = new Server();
        s.establishThreadPool(3);
        s.getClients().put("init-connection", new ClientDescriptor());
        if (args.length > 0 && args[0] != null && !args[0].isEmpty())
            s.loadEstablishConnectionScenarioFile(args[0].toString());
        else
            s.loadEstablishConnectionScenarioFile("/home/srg/java/Idea-WorkSpaces/CTI/connector_test/src/main/resources/scenarios_short1.xml");
        try {
            s.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Getters and setters
    public Map<String, ClientDescriptor> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientDescriptor> clients) {
        this.clients = clients;
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
                System.out.println("Wait");
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel clientChannel = ssc.accept();
                        logger.log(Level.FINE, "Connected ".concat(clientChannel.socket().toString()));
//                        service.submit(new ExecutionThread());
                        ExecutionThread initThread = new ExecutionThread();
                        initThread.prepareForExecution(scenarioInitiateConnection,clients.get("init-connection"),getAsyncStack(clientChannel));
                        service.execute(initThread);
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

    private AsyncTransport getAsyncStack(SocketChannel channel){
        if (stack==null) return new AsyncTransport(channel);
        else return stack;
    }
}
