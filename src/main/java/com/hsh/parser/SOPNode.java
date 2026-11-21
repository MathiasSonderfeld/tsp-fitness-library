package com.hsh.parser;


public class SOPNode extends Node {
    static int ID_COUNT = 1;


    public SOPNode() {
        super(ID_COUNT++);

    }

    @Override
    public int distance(Node n1) {
        return this.cost[n1.getId()];
    }

    @Override
    public String toString() {
        StringBuilder costToNeighbors = new StringBuilder();
        if(this.cost != null){
            for(int id = 1; id < this.cost.length; id++){
                costToNeighbors.append(String.format("%5d", this.cost[id]));
            }
        }
        return String.format("%8s |%s|", super.toString(), costToNeighbors);
    }

    @Override
    protected void setCosts(int[] cost) {
        super.setCosts(cost);
        for(int id = 1; id < cost.length; id++){
            if(cost[id] == -1){
                super.constraints.add(id);
            }
        }
    }


}
