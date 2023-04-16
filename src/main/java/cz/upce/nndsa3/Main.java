package cz.upce.nndsa3;

import cz.upce.nndsa3.structure.IndexSequentialFileBuilder;

public class Main {
    public static void main(String[] args) {
        IndexSequentialFileBuilder indexSequentialFileBuilder = new IndexSequentialFileBuilder();
        indexSequentialFileBuilder.build(10000, "data.bin", "index.csv");
    }
}
