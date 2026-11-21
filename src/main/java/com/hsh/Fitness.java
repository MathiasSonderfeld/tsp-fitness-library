package com.hsh;

import com.hsh.parser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class Fitness {
    private final ConcurrentHashMap<Integer ,Evaluable> bestOf;
    private int iteration;
    private final Dataset dataset;
    private final boolean output;
    private final boolean sop; // einmal bestimmt statt String-Vergleich pro Knoten
    private final boolean tsp;
    private int count = 0;

    public Fitness(Dataset dataset, boolean output) {
        this.dataset = dataset;
        this.bestOf = new ConcurrentHashMap<>();
        this.iteration = 0;
        this.output = output;
        this.sop = "SOP".equals(dataset.getType());
        this.tsp = "TSP".equals(dataset.getType());
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
        this.singleEvaluate(eva, iteration);
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
        return this.evaluate(evaluables.toArray(new Evaluable[0]));
    }

    public final Evaluable[] evaluate(Evaluable[] evaluables){
        for(Evaluable evaluable : evaluables){
            int length = this.process(evaluable);
            this.updateBest(this.iteration, evaluable, length);
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

    private void validate(Evaluable evaluable, ArrayList<Integer> path){
        this.validate(evaluable, path, path.size());
    }

    /**
     * Validiert nur die ersten {@code length} Knoten des Weges.
     */
    private void validate(Evaluable evaluable, ArrayList<Integer> path, int length){
        if(dataset.getSize() != length){
            evaluable.setValid(false, "Es müssen alle Knoten besucht werden! (" + length + " != " + dataset.getSize() + ")");
            return;
        }

        boolean[] visited = new boolean[dataset.getMaxId() + 1];
        for(int i = 0; i < length; i++){
            int id = path.get(i);
            Node node = dataset.getNodeByID(id);
            if(node == null){
                evaluable.setValid(false, "Unbekannter Knoten mit der ID " + id + " wurde in ihrem Weg gefunden!");
                return;
            }

            if(visited[id]){
                evaluable.setValid(false, "Der Knoten mit der ID "+ id + " wird häufiger als 1x besucht!");
                return;
            }
            visited[id] = true;

            if(this.sop){
                for(int required : node.getConstraints()){
                    if(required >= visited.length || !visited[required]){
                        evaluable.setValid(false, "Der Weg verletzt die Einschränkungen des Knoten " + id);
                        return;
                    }
                }
            }
        }

        if(this.sop){
            if(path.getFirst() != 1 || path.get(length - 1) != dataset.getSize()){
                evaluable.setValid(false, "Der Start und/oder Endknoten ist nicht korrekt!");
                return;
            }
        }

        evaluable.setValid(true, "");
    }

    /**
     * Berechnet die Fitness der ersten {@code length} Knoten des Weges (length >= 1).
     */
    private void calculateFitness(Evaluable evaluable, ArrayList<Integer> path, int length){
        boolean failed = false;
        int result = 0;
        Node prev = dataset.getNodeByID(path.getFirst());
        Node actual;
        for(int i = 1; i < length; i++){
            actual = dataset.getNodeByID(path.get(i));
            if(prev == null || actual == null){
                failed = true;
                break;
            }
            result += prev.distance(actual);
            prev = actual;
        }
        if(!failed && prev == null) failed = true; // Pfad mit nur einem (unbekannten) Knoten
        if(!failed && this.tsp) result += this.distance(path.get(length - 1), path.getFirst());
        evaluable.setFitness(!failed ? result : -1);
    }

    /**
     * Anzahl der Knoten, die zum Weg gehören. Ist der Weg als geschlossene Tour übergeben
     * (erster == letzter Knoten), zählt der letzte Knoten nicht mit. Der Weg selbst wird nicht verändert.
     */
    private static int effectiveLength(ArrayList<Integer> path){
        int size = path.size();
        if(size > 1 && path.getFirst().equals(path.get(size - 1))){
            return size - 1;
        }
        return size;
    }

    /**
     * Gemeinsamer Ablauf für alle evaluate-Varianten: Fitness berechnen und validieren.
     * @return Anzahl der Knoten, die zum Weg gehören (siehe {@link #effectiveLength})
     */
    private int process(Evaluable eva){
        ArrayList<Integer> path = eva.getPath();
        if(path == null || path.isEmpty()){
            eva.setFitness(-1);
            eva.setValid(false, "Der Weg ist leer!");
            return 0;
        }
        int length = effectiveLength(path);
        this.calculateFitness(eva, path, length);
        this.validate(eva, path, length);
        return length;
    }

    /**
     * Ist {@code candidate} besser als {@code best}? Gültige schlagen ungültige,
     * danach entscheidet die kleinere Fitness. Eine nicht berechenbare Fitness (-1) ist immer am schlechtesten.
     * Bei Gleichstand bleibt der bisherige Bestwert.
     */
    private static boolean isBetter(Evaluable candidate, Evaluable best){
        if(best == null) return true;
        if(candidate.isValid() != best.isValid()) return candidate.isValid();
        if(candidate.getFitness() < 0) return false;
        if(best.getFitness() < 0) return true;
        return candidate.getFitness() < best.getFitness();
    }

    /**
     * Kopie für bestOf: Es wird nur kopiert, wenn wirklich ein neuer Bestwert gespeichert wird.
     * Ein angehängter Rückweg zum Startknoten wird dabei weggelassen.
     */
    private static Evaluable snapshot(Evaluable eva, int length){
        ArrayList<Integer> path = eva.getPath();
        if(length == path.size()) return eva.copy();
        EvaluationResult copy = new EvaluationResult(new ArrayList<>(path.subList(0, length)));
        copy.setValid(eva.isValid(), eva.getErrorCode());
        copy.setFitness(eva.getFitness());
        return copy;
    }

    private void updateBest(int iteration, Evaluable eva, int length){
        this.bestOf.compute(iteration, (key, best) -> isBetter(eva, best) ? snapshot(eva, length) : best);
    }

    public final int distance(double x1, double y1, double x2, double y2){
        double xd = x1 - x2;
        double yd = y1 - y2;
        return (int) (Math.sqrt(xd * xd + yd * yd) + 0.5);
    }

    public final int distance(int id1, int id2){
        return this.dataset.getNodeByID(id1).distance(this.dataset.getNodeByID(id2));
    }

    private void printBest(int iteration){
        if(this.bestOf.get(iteration) == null) return;
        System.out.printf("Iteration: %5d -- Fitness: %3d -- Valid: %6s -- Path: %s%n",iteration, this.bestOf.get(iteration).getFitness(), this.bestOf.get(iteration).isValid(), this.bestOf.get(iteration).getPath());
    }

    public void finish(){
        for(Integer i : new TreeSet<>(bestOf.keySet())){
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

    private void singleEvaluate(Evaluable eva, int iteration){
        if(iteration >= 0) count++;
        int length = this.process(eva);
        if(iteration >= 0) this.updateBest(iteration, eva, length);
    }

    public int getCount() {
        return count;
    }
}
