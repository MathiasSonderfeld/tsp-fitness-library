package com.hsh.parser;

import java.util.ArrayList;
import java.util.HashMap;

public class Dataset {
    private String type, name;
    private int size;
    private HashMap<Integer, Node> nodes;

    public Dataset(String type, int size, String name) {
        this.type = type;
        this.size = size;
        this.name = name;
        this.nodes = new HashMap<>();
    }

    protected void addNode(String row){
        Node node;
        String[] splittedRow = row.trim().split("\\s+");
        if(type.equals("TSP")){
            node = new TSPNode(
                    Double.parseDouble(splittedRow[1]), // x
                    Double.parseDouble(splittedRow[2]), // y
                    Integer.parseInt(splittedRow[0])    // id
            );
        }else{
            node = new SOPNode();
            for(int i = 0; i < splittedRow.length; i++){
                node.setCost(Integer.parseInt(splittedRow[i]), i + 1);
            }
        }
        this.nodes.put(node.getId(), node);
    }

    public Node getNodeByID(int id){
        return this.nodes.get(id);
    }

    public int getSize(){
        return this.nodes.size();
    }

    public String getType() {
        return type;
    }

    public Node[] getNodes(){
        return this.nodes.values().toArray(this.type.equals("SOP") ? new SOPNode[0] : new TSPNode[0]);
    }

    @Override
    public String toString() {
        String nodesAsString = "";
        for(Node node : nodes.values()){
            nodesAsString += node.toString() + "\n";
        }
        return "Dataset{\n" +
                " type='" + type + '\'' +
                ",\n name='" + name + '\'' +
                ",\n size=" + size + ",\n"+
                " Nodes: \n" +
                    nodesAsString +
                '}';
    }
}
