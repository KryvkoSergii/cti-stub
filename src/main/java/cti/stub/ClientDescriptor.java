package cti.stub;

import cti.stub.exceptions.ConnectorException;
import cti.stub.model.ScenarioPairContainer;
import cti.stub.model.VariablesDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by srg on 04.07.16.
 */
public class ClientDescriptor {
    private Map<String, byte[]> variableContainer = new ConcurrentHashMap<String, byte[]>();
    private String[] clientState = new String[3];


    //Getters and setters
    public Map<String, byte[]> getVariableContainer() {
        return variableContainer;
    }

    public void setVariableContainer(Map<String, byte[]> variableContainer) {
        this.variableContainer = variableContainer;
    }

    public String[] getClientState() {
        return clientState;
    }

    public void setClientState(String[] clientState) {
        this.clientState = clientState;
    }


    //Methods
    //Static methods

    /**
     * Производит процесс
     * @param scenarioFilePath
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Map<String, Object> parseScenarioContainer(String scenarioFilePath) throws ParserConfigurationException,
            IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(scenarioFilePath));
        Map<String, Object> result = getSubNode(doc, 0);
        return result;
    }

    /**
     * производит конвертацию
     * @param node
     * @param level
     * @return
     */
    private static Map<String, Object> getSubNode(Node node, int level) {
        Map<String, Object> tmp = new ConcurrentHashMap<String, Object>();
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node currentNode = list.item(i); // текущий нод
            if (currentNode.hasChildNodes() && currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //проверка есть ли внутренние put/get
                String name = currentNode.getChildNodes().item(1).getNodeName();
                if (name.equals("GET") || name.equals("PUT")) {
                    tmp.put(currentNode.getNodeName(), getPair(currentNode.getChildNodes()));
                } else {
                    tmp.put(currentNode.getNodeName(), getSubNode(currentNode, level + 1));
                }
            }
        }
        return tmp;
    }

    private static List<ScenarioPairContainer> getPair(NodeList list) {
        List<ScenarioPairContainer> tmp = new ArrayList<ScenarioPairContainer>();
        byte b;
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                b = (byte) (n.getNodeName().equals("GET") ? 0 : 1);
//                System.out.println(n.getChildNodes().item(0).getNodeValue());
                tmp.add(new ScenarioPairContainer(b, (String) n.getChildNodes().item(0).getNodeValue()));
            }
        }
        return tmp;
    }

    /**
     * просматривает сценарий на наличие неизменяемых значений GET/PUT (отсуствие переменных), пересоздает Map<String, Object> с представлением значений
     * GET/PUT в виде массива байт.
     *
     * @return
     */
    public static Map<String, Object> preCompile(Map<String, Object> rawScenario) {
        Set<Map.Entry<String, Object>> root = rawScenario.entrySet();
        for (Map.Entry<String, Object> m : root) {
            if (m.getValue() instanceof Map) {
                // есть вложенненность представлення в Map. рекурсивный вызов
                preCompile((Map) m.getValue());
            } else if (m.getValue() instanceof List) {
                for (ScenarioPairContainer spc : (List<ScenarioPairContainer>) m.getValue()) {
                    if (!(spc.getCommand() instanceof byte[]) &&
                            !(((String) spc.getCommand()).contains("@") || ((String) spc.getCommand()).contains("#") || ((String) spc.getCommand()).contains("$"))) {
                        spc.setCommand(hexStringToByteArray(spc.getCommand().toString()));
                    } else{
                        parseAndCompileVariables(spc);
                    }
                }
            } else try {
                throw new ConnectorException("Scenario casting exception");
            } catch (ConnectorException e) {
                System.out.println(e.getMessage());
            }
        }
        return rawScenario;
    }

    public static byte[] hexStringToByteArray(String hexInString) {
        int len = hexInString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexInString.charAt(i), 16) << 4) + Character.digit(hexInString.charAt(i + 1), 16));
        }
        return data;
    }

    private static ScenarioPairContainer parseAndCompileVariables(ScenarioPairContainer scp) {
        StringTokenizer st = new StringTokenizer((String) scp.getCommand(), ":");
        String token;
        int begin, length;
        //позиция
        int positionInArray = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.contains("#")) {
                String name = token.substring(1, token.indexOf("("));
                begin = Integer.valueOf(token.substring(token.indexOf("(") + 1, token.indexOf(";")));
                length = Integer.valueOf(token.substring(token.indexOf(";") + 1, token.indexOf(")")));
                scp.getVariables().add(new VariablesDescriptor(name, positionInArray, (byte) 1, begin, length));
                scp.getInBytes().add(new byte[0]);
            } else if(token.contains("$")){
                String name = token.substring(1, token.indexOf("("));
                begin = Integer.valueOf(token.substring(token.indexOf("(") + 1, token.indexOf(";")));
                length = Integer.valueOf(token.substring(token.indexOf(";") + 1, token.indexOf(")")));
                scp.getVariables().add(new VariablesDescriptor(name, positionInArray, (byte) 2, begin, length));
                scp.getInBytes().add(new byte[0]);
            } else if (token.contains("@")){
                String name = token.substring(1, token.indexOf("("));
                begin = Integer.valueOf(token.substring(token.indexOf("(") + 1, token.indexOf(";")));
                length = Integer.valueOf(token.substring(token.indexOf(";") + 1, token.indexOf(")")));
                scp.getVariables().add(new VariablesDescriptor(name, positionInArray, (byte) 3, begin, length));
                scp.getInBytes().add(new byte[0]);
            }
            else {
                scp.getInBytes().add(hexStringToByteArray(token));
            }
            positionInArray++;
        }
        return scp;
    }


}
