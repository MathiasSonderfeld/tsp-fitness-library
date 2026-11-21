package com.hsh;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Evaluable {
    private boolean valid;
    private String errorCode;
    private int fitness;

    public Evaluable() {
        this.valid = true;
        this.fitness = -1;
        this.errorCode = "";
    }

    public abstract ArrayList<Integer> getPath();

    protected final void setValid(boolean valid, String code) {
        this.valid = valid;
        this.errorCode = code;
    }

    public final int getFitness() {
        return fitness;
    }

    protected final void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public final boolean isValid() {
        return valid;
    }

    protected final Evaluable copy(){
        EvaluationResult eva = new EvaluationResult(this.getPath());
        eva.setValid(this.isValid(), this.errorCode);
        eva.setFitness(this.fitness);
        return eva;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluable evaluable = (Evaluable) o;
        return valid == evaluable.valid &&
                Double.compare(evaluable.fitness, fitness) == 0 &&
                Objects.equals(errorCode, evaluable.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errorCode, fitness);
    }

    @Override
    public String toString() {
        return "Evaluable{" +
                "valid=" + valid +
                ", errorCode='" + errorCode + '\'' +
                ", fitness=" + fitness +
                '}';
    }
}
