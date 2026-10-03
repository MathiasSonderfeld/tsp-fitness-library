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
        // euclidean Distance !
        if(!this.cost.containsKey(n1.getId())) {
            double xd = 0;
            double yd = 0;
            xd = x - ((TSPNode) n1).getX();
            yd = y - ((TSPNode) n1).getY();
            int result = (int) (Math.sqrt(xd * xd + yd * yd) + 0.5);
            this.setCost(result, n1.getId());
            return result;
        }
        return this.cost.get(n1.getId());
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
