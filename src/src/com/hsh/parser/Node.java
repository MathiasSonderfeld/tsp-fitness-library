package com.hsh.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Node {
    private int id;
    protected ConcurrentHashMap<Integer, Integer> cost;
    protected ArrayList<Integer> constraints;

    public Node(int id) {
        this.id = id;
        this.cost = new ConcurrentHashMap<>();
        this.constraints = new ArrayList<>();
    }

    public abstract int distance(Node n1);

    protected void setCost(int cost, int id){
        this.cost.put(id, cost);
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
