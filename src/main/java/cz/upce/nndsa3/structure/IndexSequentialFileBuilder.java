package cz.upce.nndsa3.structure;

import cz.upce.nndsa3.data.Product;
import cz.upce.nndsa3.util.ProductGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cz.upce.nndsa3.util.ProductGenerator.MAX_STRING_LENGTH;
import static cz.upce.nndsa3.util.ProductGenerator.PRODUCT_BYTE_SIZE;

public class IndexSequentialFileBuilder {

    private static final int HEADER_SIZE = 16;

    public void build(int numberOfRecords, String dataFileName, String indexFileName) {
        List<Product> generatedProducts = ProductGenerator.generateProducts(numberOfRecords);
        Collections.sort(generatedProducts);
        saveToDataFile(generatedProducts, dataFileName, indexFileName);
    }

    private void saveToDataFile(List<Product> generatedProducts, String dataFileName, String indexFileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(dataFileName);
            try (DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
                writeControlBlock(generatedProducts, dataOutputStream, indexFileName);
                for (Product product : generatedProducts) {

                    String productCode = product.getCode();
                    if (productCode.length() < MAX_STRING_LENGTH) {
                        productCode += " ".repeat(MAX_STRING_LENGTH - productCode.length());
                    }


                    dataOutputStream.writeInt(product.getId());
                    dataOutputStream.writeChars(productCode);
                }
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeControlBlock(List<Product> generatedProducts, DataOutputStream dataOutputStream, String indexFileName) throws IOException {
        int blockSize = (int) Math.sqrt(generatedProducts.size());
        int numberOfBlocks = generatedProducts.size() / blockSize;
        int sectionSize = blockSize * PRODUCT_BYTE_SIZE;
        dataOutputStream.writeInt(blockSize);
        dataOutputStream.writeInt(numberOfBlocks);
        dataOutputStream.writeInt(sectionSize);
        dataOutputStream.writeInt(HEADER_SIZE);
        saveToIndexFile(generatedProducts, indexFileName, blockSize);
    }

    private void saveToIndexFile(List<Product> generatedProducts, String indexFileName, int blockSize) {
        try {
            try (FileWriter fileWriter = new FileWriter(indexFileName)) {
                for (int i = 0; i < generatedProducts.size(); i += blockSize) {
                    fileWriter.write(Integer.toString(generatedProducts.get(i).getId()));
                    fileWriter.write(",");
                    fileWriter.write(Integer.toString(HEADER_SIZE + i * PRODUCT_BYTE_SIZE));
                    fileWriter.write("\n");

                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Product findProduct(){
        return null;
    }
}
