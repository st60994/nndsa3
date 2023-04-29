package cz.upce.nndsa3.structure;

import cz.upce.nndsa3.data.Product;
import cz.upce.nndsa3.util.BlockLogger;
import cz.upce.nndsa3.util.ProductGenerator;

import java.io.*;
import java.util.*;

import static cz.upce.nndsa3.util.ProductGenerator.MAX_STRING_LENGTH;
import static cz.upce.nndsa3.util.ProductGenerator.PRODUCT_BYTE_SIZE;

public class IndexSequentialFileBuilder {

    private static final int HEADER_SIZE = 16;
    private static final int INDEX_HEADER_SIZE = 8;
    private File indexFile;
    private File dataFile;
    private int sectionSize;

    public IndexSequentialFileBuilder(String dataFileName, String indexFileName) {
        this.dataFile = new File(dataFileName);
        this.indexFile = new File(indexFileName);
    }

    public void build(int numberOfRecords) {
        List<Product> generatedProducts = ProductGenerator.generateProducts(numberOfRecords);
        Collections.sort(generatedProducts);
        saveToDataFile(generatedProducts);
    }

    private void saveToDataFile(List<Product> generatedProducts) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            try (DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
                writeControlBlock(generatedProducts, dataOutputStream, indexFile);
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

    private void writeControlBlock(List<Product> generatedProducts, DataOutputStream dataOutputStream, File indexFile) throws IOException {
        int blockSize = (int) Math.sqrt(generatedProducts.size());
        int numberOfBlocks = generatedProducts.size() / blockSize;
        int sectionSize = blockSize * PRODUCT_BYTE_SIZE;
        dataOutputStream.writeInt(HEADER_SIZE);
        dataOutputStream.writeInt(blockSize);
        dataOutputStream.writeInt(numberOfBlocks);
        dataOutputStream.writeInt(sectionSize);
        saveToIndexFile(generatedProducts, indexFile, blockSize, numberOfBlocks);
    }

    private void saveToIndexFile(List<Product> generatedProducts, File indexFile, int blockSize, int numberOfRecords) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(indexFile);
            try (DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
                dataOutputStream.writeInt(INDEX_HEADER_SIZE);
                dataOutputStream.writeInt(numberOfRecords);
                for (int i = 0; i < generatedProducts.size(); i += blockSize) {
                    int id = generatedProducts.get(i).getId();
                    dataOutputStream.writeInt(generatedProducts.get(i).getId());
                    dataOutputStream.writeInt(HEADER_SIZE + i * PRODUCT_BYTE_SIZE);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Product findProduct(int key) {
        Map<Integer, Integer> indexKeyPosition = readIndexValues();
        List<Integer> keys = new ArrayList<>(indexKeyPosition.keySet());
        int firstSearchIndex = 0;
        int lastSearchIndex = indexKeyPosition.size() - 1;
        int middleIndex = -1;
        int currentKey = -1;
        while (firstSearchIndex <= lastSearchIndex) {
            middleIndex = (firstSearchIndex + lastSearchIndex) / 2;
            currentKey = keys.get(middleIndex);
            BlockLogger.writeIndexFileToLog(middleIndex, currentKey);

            if (key < currentKey) {
                lastSearchIndex = middleIndex - 1;
            } else if (key > currentKey) {
                firstSearchIndex = middleIndex + 1;
            } else {
                int position = indexKeyPosition.get(currentKey);
                return findProductInDataBlock(position, key); // TODO vím že je hned první není potřeba dělat binary search
            }
        }
        if (key > currentKey) {
            int position = indexKeyPosition.get(currentKey);
            return findProductInDataBlock(position, key);
        }
        int position = indexKeyPosition.get(keys.get(middleIndex - 1));
        return findProductInDataBlock(position, key);
    }

    private Product findProductInDataBlock(int position, int key) {
        List<Product> products = readWholeBlock(position);
        int firstSearchIndex = 0;
        int lastSearchIndex = products.size() - 1;
        int middleIndex;
        int currentKey;
        while (firstSearchIndex <= lastSearchIndex) {
            middleIndex = (firstSearchIndex + lastSearchIndex) / 2;
            currentKey = products.get(middleIndex).getId();
            BlockLogger.writeDataFileToLog(middleIndex, currentKey);
            if (key < currentKey) {
                lastSearchIndex = middleIndex - 1;
            } else if (key > currentKey) {
                firstSearchIndex = middleIndex + 1;
            } else {
                return products.get(middleIndex);
            }
        }
        return null;
    }

    public List<Integer> getAllKeys() {
        List<Integer> keysList = new ArrayList<>();
        Map<Integer, Integer> indexKeyPosition = readIndexValues();

        for (Integer location : indexKeyPosition.values()) {
            System.out.println(location);
            List<Product> productBlock = readWholeBlock(location);
            for (Product product : productBlock) {
                keysList.add(product.getId());
            }
        }

        return keysList;
    }

    private Map<Integer, Integer> readIndexValues() {
        Map<Integer, Integer> indexKeyPositionMap = new LinkedHashMap<>();
        try (RandomAccessFile indexAccessFile = new RandomAccessFile(indexFile, "r")) {
            int headerSize = indexAccessFile.readInt();
            int numberOfRecords = indexAccessFile.readInt();
            for (int i = 0; i < numberOfRecords; i++) {
                int beginningKey = indexAccessFile.readInt();
                int position = indexAccessFile.readInt();
                indexKeyPositionMap.put(beginningKey, position);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return indexKeyPositionMap;
    }

    private List<Product> readWholeBlock(long position) {
        List<Product> products = new ArrayList<>();
        try (RandomAccessFile dataAccessFile = new RandomAccessFile(dataFile, "r")) {
            dataAccessFile.readInt(); // skip header
            int blockSize = dataAccessFile.readInt();
            dataAccessFile.readInt(); // skip number of blocks
            int sectionSize = dataAccessFile.readInt();
            BlockLogger.writeDataBlockAccessToLog(position, (int) (position / sectionSize));
            dataAccessFile.seek(position);
            for (int i = 0; i < blockSize; i++) {
                int id = dataAccessFile.readInt();
                StringBuilder code = new StringBuilder();
                for (int j = 0; j < 16; j++) {
                    code.append(dataAccessFile.readChar());
                }
                products.add(new Product(id, code.toString()));
            }
            System.out.println(dataAccessFile.getFilePointer());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return products;
    }
}
