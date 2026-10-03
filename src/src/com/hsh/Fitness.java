package com.hsh;

import com.hsh.parser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Fitness {
    private ConcurrentHashMap<Integer ,Evaluable> bestOf;
    private int iteration;
    private Dataset dataset;
    private boolean output = true;
    private int count = 0;

    public Fitness(Dataset dataset, boolean output) {
        this.dataset = dataset;
        this.bestOf = new ConcurrentHashMap<>();
        this.iteration = 0;
        this.output = output;
    }

    public Fitness(Dataset dataset) {
        this(dataset, true);
    }

    public Fitness(String pathToData) throws IOException{
        this(Parser.read(pathToData), true);
    }

    public Fitness(String pathToData, boolean output) throws IOException{
        this(Parser.read(pathToData), output);
    }

    public final Evaluable evaluate(Evaluable eva, int iteration){
        ArrayList<Integer> path = eva.getPath();
        this.singleEvaluate(eva, path, iteration);
        return eva;
    }

    public final Evaluable evaluate(ArrayList<Integer> evaluable, int iteration){
        EvaluationResult evaluationResult = new EvaluationResult(evaluable);
        return this.evaluate(evaluationResult, iteration);
    }

    public final Evaluable evaluate(int[] evaluable, int iteration){
        EvaluationResult evaluationResult = new EvaluationResult(evaluable);
        return this.evaluate(evaluationResult, iteration);
    }

    public final Evaluable[] evaluate(int[][] evaluables){
        ArrayList<Evaluable> evaluable = new ArrayList<>();
        for(int[] path : evaluables){
            evaluable.add(new EvaluationResult(path));
        }
        return this.evaluate(evaluable);
    }

    public final Evaluable[] evaluate(ArrayList<Evaluable> evaluables){
        return this.evaluate(evaluables.toArray(new Evaluable[evaluables.size()]));
    }

    public final Evaluable[] evaluate(Evaluable[] evaluables){
        Consumer<Evaluable> calc = (Evaluable iterationResult) -> {
            ArrayList<Integer> path = iterationResult.getPath();
            if(path.get(0) == path.get(path.size() - 1)){
                path.remove(path.size() - 1);
            }
            calculateFitness(iterationResult, path);
            validate(iterationResult, path);
        };
        Arrays.stream(evaluables).forEach(calc);

        for(Evaluable evaluable : evaluables){
            if(this.bestOf.get(this.iteration) == null) {
                this.bestOf.put(this.iteration, evaluable.copy());
                continue;
            }

            if(!this.bestOf.get(iteration).isValid() && evaluable.isValid()) {
                this.bestOf.put(iteration, evaluable.copy());
                continue;
            }

            if(this.bestOf.get(this.iteration).getFitness() > evaluable.getFitness()){
                if(evaluable.isValid() || (!evaluable.isValid() && !this.bestOf.get(iteration).isValid())) {
                    this.bestOf.put(this.iteration, evaluable.copy());
                }
            }
        }
        if(this.output) this.printBest(this.iteration);
        count += evaluables.length;
        this.iteration++;
        return evaluables;
    }

    public final Evaluable validate(Evaluable evaluable){
        this.validate(evaluable, evaluable.getPath());
        return evaluable;
    }

    public final Evaluable validate(int[] path){
        EvaluationResult eva = new EvaluationResult(path);
        this.validate(eva, eva.getPath());
        return eva;
    }

    private final void validate(Evaluable evaluable, ArrayList<Integer> path){
        if(dataset.getSize() != path.size()){
            evaluable.setValid(false, "Es müssen alle Knoten besucht werden! (" + path.size() + " != " + dataset.getSize() + ")");
            return;
        }

        HashMap<Integer, Boolean> dublicates = new HashMap<>();
        for(Integer id : path){
            if(dublicates.containsKey(id)){
                evaluable.setValid(false, "Der Knoten mit der ID "+ id + " wird häufiger als 1x besucht!");
                return;
            }
            dublicates.put(id, true);

            if(dataset.getNodeByID(id) == null){
                evaluable.setValid(false, "Unbekannter Knoten mit der ID " + id + " wurde in ihrem Weg gefunden!");
                return;
            }

            if(this.dataset.getType().equals("SOP")){
                if(!dublicates.keySet().containsAll(((SOPNode)this.dataset.getNodeByID(id)).getConstraints())){
                    evaluable.setValid(false, "Der Weg verletzt die Einschränkungen des Knoten " + id);
                    return;
                }
            }
        }

        if(this.dataset.getType().equals("SOP")){
            if(path.get(0) != 1 || path.get(path.size() - 1) != dataset.getSize()){
                evaluable.setValid(false, "Der Start und/oder Endknoten ist nicht korrekt!");
                return;
            }
        }

        evaluable.setValid(true, "");
    }

    private final void calculateFitness(Evaluable evaluable, ArrayList<Integer> path){
        boolean failed = false;
        int result = 0;
        Node prev = dataset.getNodeByID(path.get(0));
        Node actual = null;
        for(int i = 1; i < path.size(); i++){
            actual = dataset.getNodeByID(path.get(i));
            if(prev == null || actual == null){
                failed = true;
                break;
            }
            result += prev.distance(actual);
            prev = actual;
        }
        if(!failed && this.dataset.getType().equals("TSP")) result += this.distance(path.get(path.size() - 1), path.get(0));
        evaluable.setFitness(!failed ? result : -1);
    }

    public final int distance(double x1, double y1, double x2, double y2){
        double xd = x1 - x2;
        double yd = y1 - y2;
        return (int) (Math.sqrt(xd * xd + yd * yd) + 0.5);
    }

    public final int distance(int id1, int id2){
        return this.dataset.getNodeByID(id1).distance(this.dataset.getNodeByID(id2));
    }

    private final void printBest(int iteration){
        System.out.println(String.format("Iteration: %5d -- Fitness: %3d -- Valid: %6s -- Path: %s",iteration, this.bestOf.get(iteration).getFitness(), this.bestOf.get(iteration).isValid(), this.bestOf.get(iteration).getPath()));
    }

    public void finish(){
        for(Integer i : bestOf.keySet()){
            printBest(i);
        }
    }

    public Evaluable getBest(int iteration) {
        return this.bestOf.get(iteration) != null ? this.bestOf.get(iteration).copy() : null;
    }

    public ArrayList<Evaluable> getBests(){
        ArrayList<Evaluable> toReturn = new ArrayList<>();
        for(Evaluable best : this.bestOf.values()){
            toReturn.add(best.copy());
        }
        return toReturn;
    }

    /**
     * @return Evaluable oder Null (falls kein valides Ergebnis vorhanden)
     */
    public Evaluable getAbsolutBest(){
        Evaluable toReturn = null;
        for(Evaluable best : this.bestOf.values()){
            if(toReturn == null && best.isValid()) {
                toReturn = best;
                continue;
            }
            if(best.isValid() && best.getFitness() < toReturn.getFitness()){
                toReturn = best;
            }
        }
        return toReturn != null ? toReturn.copy() : null;
    }

    public Dataset getDataset() {
        return dataset;
    }

    private Evaluable singleEvaluate(Evaluable eva, ArrayList<Integer> path, int iteration){
        if(iteration >= 0) count++;
        if(path.get(0) == path.get(path.size() - 1)){
            path.remove(path.size() - 1);
        }
        calculateFitness(eva, path);
        validate(eva, path);

        if( iteration >= 0){
            if(this.bestOf.get(iteration) == null){
                this.bestOf.put(iteration, eva.copy());
            }else if(this.bestOf.get(iteration).getFitness() > eva.getFitness()){
                this.bestOf.put(iteration, eva.copy());
            }
        }

        return eva;
    }

    public int getCount() {
        return count;
    }
}
