package cz.upce.nndsa3;

import cz.upce.nndsa3.structure.IndexSequentialFileBuilder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        IndexSequentialFileBuilder indexSequentialFileBuilder = new IndexSequentialFileBuilder("data.bin", "index.index");
        indexSequentialFileBuilder.build(10000);
        indexSequentialFileBuilder.findProductSequential(123);
//        List<Integer> list = indexSequentialFileBuilder.getAllKeys();
//        System.out.println(list.toString());
//        System.out.println(indexSequentialFileBuilder.findProduct(77401));
    }
}
