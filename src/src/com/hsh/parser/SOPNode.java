package com.hsh.parser;

import java.util.ArrayList;

public class SOPNode extends Node {
    static int ID_COUNT = 1;


    public SOPNode() {
        super(ID_COUNT++);

    }

    @Override
    public int distance(Node n1) {
        return super.cost.get(n1.getId());
    }

    @Override
    public String toString() {
        String costToNeighbors = "";
        for(int cost : this.cost.values()){
            costToNeighbors += String.format("%5d" ,cost);
        }
        return String.format("%8s |%s|", super.toString(), costToNeighbors);
    }

    @Override
    protected void setCost(int cost, int id) {
        super.setCost(cost, id);
        if(cost == -1){
            super.constraints.add(id);
        }
    }


}
