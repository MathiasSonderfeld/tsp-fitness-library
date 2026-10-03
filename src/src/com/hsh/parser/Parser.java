package com.hsh.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Parser {
    public static Dataset read(String path) throws IOException{
        Path filePath = Paths.get(path);
        boolean startReadingCoords = false;
        String row = null;
        Scanner lineScanner = new Scanner(filePath);

        String name = lineScanner.nextLine().split(":")[1].trim();
        String tmp = lineScanner.nextLine();
        String type = "";
        if(tmp.contains("TYPE")){
            type = tmp.split(":")[1].trim();
            lineScanner.nextLine();
        }else{
            type = lineScanner.nextLine().split(":")[1].trim();
        }
        int size = Integer.parseInt(lineScanner.nextLine().split(":")[1].trim());

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
                if(row.indexOf("EOF") < 0) {
                    dataset.addNode(row);
                }
            }
        }
        lineScanner.close();

        return dataset;
    }
}
