package cti.stub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by srg on 04.07.16.
 */
public class ScenarioPairContainer<T> {
    private byte method;
    private T command;
    private List<VariablesDescriptor> variables = new ArrayList<VariablesDescriptor>();
    private List<byte[]> inBytes = new ArrayList<byte[]>();


    //Constructors
    public ScenarioPairContainer(byte method, T command) {
        this.method = method;
        this.command = command;
    }


    //Getters and Setters
    public byte getMethod() {
        return method;
    }

    public void setMethod(byte method) {
        this.method = method;
    }

    public T getCommand() {
        return command;
    }

    public void setCommand(T command) {
        this.command = command;
    }

    public List<VariablesDescriptor> getVariables() {
        return variables;
    }

    public void setVariables(List<VariablesDescriptor> variables) {
        this.variables = variables;
    }

    public List<byte[]> getInBytes() {
        return inBytes;
    }

    public void setInBytes(List<byte[]> inBytes) {
        this.inBytes = inBytes;
    }

    //Methods
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScenarioPairContainer{");
        sb.append("method=").append(method);
        sb.append(", command=").append(command);
        sb.append('}');
        return sb.toString();
    }
}
