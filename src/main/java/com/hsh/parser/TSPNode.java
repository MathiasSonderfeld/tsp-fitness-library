package com.hsh.parser;

import java.text.DecimalFormat;

public class TSPNode extends Node {
    private double x, y;

    public TSPNode(double x, double y, int id) {
        super(id);
        this.x = x;
        this.y = y;
    }

    @Override
    public int distance(Node n1) {
        int id = n1.getId();
        if(this.cost != null && id >= 0 && id < this.cost.length){
            return this.cost[id]; // vorberechnet (siehe Dataset.build)
        }
        return this.computeDistance((TSPNode) n1); // z.B. Knoten, der nicht zu einem Dataset gehört
    }

    /**
     * euclidean Distance, gerundet
     */
    int computeDistance(TSPNode other){
        double xd = x - other.x;
        double yd = y - other.y;
        return (int) (Math.sqrt(xd * xd + yd * yd) + 0.5);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {

        return String.format("%8s %8s %8s", super.toString(), "x=" + new DecimalFormat("#.0#").format(x), "y=" + new DecimalFormat("#.0#").format(y));
    }
}
