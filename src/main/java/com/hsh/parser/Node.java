package com.hsh.parser;

import java.util.ArrayList;

public abstract class Node {
    private final int id;
    /** cost[id] = Kosten zum Knoten mit dieser ID. Wird einmal beim Einlesen gesetzt und ist danach nur noch lesend. */
    protected int[] cost;
    protected ArrayList<Integer> constraints;

    public Node(int id) {
        this.id = id;
        this.constraints = new ArrayList<>();
    }

    public abstract int distance(Node n1);

    protected void setCosts(int[] cost){
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Integer> getConstraints() {
        return new ArrayList<>(this.constraints);
    }

    @Override
    public String toString() {
        return "id= " + id;
    }
}
