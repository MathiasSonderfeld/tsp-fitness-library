package com.hsh.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Parser {
    public static Dataset read(String path) throws IOException{
        Path filePath = Paths.get(path);
        boolean startReadingCoords = false;
        String row;

        try (Scanner lineScanner = new Scanner(filePath)) {
            String name = headerValue(lineScanner.nextLine(), path);
            String tmp = lineScanner.nextLine();
            String type;
            if(tmp.contains("TYPE")){
                type = headerValue(tmp, path);
                lineScanner.nextLine();
            }else{
                type = headerValue(lineScanner.nextLine(), path);
            }
            if(!type.equals("TSP") && !type.equals("SOP")){
                throw new IOException("Unbekannter Datensatz-Typ '" + type + "' in " + path + " (erwartet: TSP oder SOP)");
            }

            int size;
            String sizeValue = headerValue(lineScanner.nextLine(), path);
            try {
                size = Integer.parseInt(sizeValue);
            } catch (NumberFormatException e) {
                throw new IOException("Ungültige Anzahl an Knoten '" + sizeValue + "' in " + path, e);
            }
            if(size <= 0){
                throw new IOException("Die Anzahl der Knoten muss größer als 0 sein, ist aber " + size + " (" + path + ")");
            }

            Dataset dataset = new Dataset(type, size, name);

            while (lineScanner.hasNext()) {
                row = lineScanner.nextLine();
                if(!startReadingCoords){
                    if(type.equals("TSP")){
                       startReadingCoords = row.contains("NODE_COORD_SECTION");
                    }else{
                       startReadingCoords = row.contains(size+"");
                    }
                }else {
                    if(!row.contains("EOF") && !row.trim().isEmpty()) {
                        dataset.addNode(row);
                    }
                }
            }

            if(dataset.getSize() != size){
                throw new IOException("Der Header von " + path + " kündigt " + size + " Knoten an, gelesen wurden aber " + dataset.getSize());
            }

            dataset.build();
            return dataset;
        } catch (NoSuchElementException e) {
            throw new IOException("Unvollständiger Header in " + path, e);
        }
    }

    /**
     * Liefert den Teil hinter dem ersten ':' einer Headerzeile, z.B. "TYPE : TSP" -> "TSP".
     */
    private static String headerValue(String line, String path) throws IOException{
        int colon = line.indexOf(':');
        if(colon < 0){
            throw new IOException("Ungültige Headerzeile '" + line + "' in " + path + " (':' fehlt)");
        }
        return line.substring(colon + 1).trim();
    }
}
