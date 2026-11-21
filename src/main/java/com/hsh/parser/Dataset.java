package com.hsh.parser;

import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("unused")
public class Dataset {
    private final String type;
    private final String name;
    private final int size;
    private final HashMap<Integer, Node> nodes;
    private Node[] nodesById; // nach build(): Index = ID, nur noch lesend
    private int maxId;

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
            int[] cost = new int[splittedRow.length + 1]; // Index = ID des Zielknotens (1-basiert)
            for(int i = 0; i < splittedRow.length; i++){
                cost[i + 1] = Integer.parseInt(splittedRow[i]);
            }
            node.setCosts(cost);
        }
        this.nodes.put(node.getId(), node);
    }

    /**
     * Berechnet nach dem Einlesen aller Knoten die Kosten vor (TSP: Distanzmatrix).
     * Danach wird nichts mehr verändert, der Datensatz ist rein lesend nutzbar.
     */
    protected void build() throws IOException{
        int maxId = 0;
        for(Node node : this.nodes.values()){
            if(node.getId() < 0){
                throw new IOException("Negative Knoten-ID " + node.getId() + " in Datensatz " + name);
            }
            maxId = Math.max(maxId, node.getId());
        }

        Node[] byId = new Node[maxId + 1];
        for(Node node : this.nodes.values()){
            byId[node.getId()] = node;
        }
        this.nodesById = byId;
        this.maxId = maxId;

        if(!type.equals("TSP")) return;

        TSPNode[] all = this.nodes.values().toArray(new TSPNode[0]);
        for(TSPNode node : all){
            node.setCosts(new int[maxId + 1]);
        }
        for(int i = 0; i < all.length; i++){
            for(int j = i + 1; j < all.length; j++){
                int distance = all[i].computeDistance(all[j]);
                all[i].cost[all[j].getId()] = distance;
                all[j].cost[all[i].getId()] = distance;
            }
        }
    }

    public Node getNodeByID(int id){
        Node[] byId = this.nodesById;
        if(byId == null){
            return this.nodes.get(id); // noch nicht gebaut
        }
        return (id >= 0 && id < byId.length) ? byId[id] : null;
    }

    /**
     * @return größte vorkommende Knoten-ID (gültig nach dem Einlesen)
     */
    public int getMaxId(){
        return this.maxId;
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
        StringBuilder nodesAsString = new StringBuilder();
        for(Node node : nodes.values()){
            nodesAsString.append(node.toString()).append("\n");
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
