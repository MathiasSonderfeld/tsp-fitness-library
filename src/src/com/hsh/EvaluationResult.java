package com.hsh;

import java.util.ArrayList;

public class EvaluationResult extends Evaluable {
    private ArrayList<Integer> path;

    public EvaluationResult(int[] path) {
        this.path = new ArrayList<>();
        for(int l : path){
            this.path.add(l);
        }
    }

    public EvaluationResult(ArrayList<Integer> path) {
        this.path = new ArrayList<>();
        this.path.addAll(path);
    }

    @Override
    public ArrayList<Integer> getPath() {
        return this.path;
    }
}
