package cz.upce.nndsa3.structure;

import cz.upce.nndsa3.data.*;
import cz.upce.nndsa3.util.BlockLogger;
import cz.upce.nndsa3.util.ProductGenerator;

import java.io.*;
import java.util.*;

import static cz.upce.nndsa3.util.ProductGenerator.MAX_STRING_LENGTH;
import static cz.upce.nndsa3.util.ProductGenerator.PRODUCT_BYTE_SIZE;

public class IndexSequentialFileBuilder {

    private static final int HEADER_SIZE = 16;
    private static final int INDEX_HEADER_SIZE = 16;
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
            try (ObjectOutputStream dataOutputStream = new ObjectOutputStream(fileOutputStream)) {
                int blockSize = (int) Math.sqrt(generatedProducts.size());
                writeControlBlock(generatedProducts, dataOutputStream, indexFile, blockSize);
                List<Product> products = new ArrayList<>();
                for (Product product : generatedProducts) {

                    String productCode = product.getCode();
                    if (productCode.length() < MAX_STRING_LENGTH) {
                        productCode += " ".repeat(MAX_STRING_LENGTH - productCode.length());
                    }
                    product.setCode(productCode);
                    products.add(product);
                    if (products.size() == blockSize) {
                        dataOutputStream.writeObject(new DataBlock(products));
                        products = new ArrayList<>();
                    }
                }
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeControlBlock(List<Product> generatedProducts, ObjectOutputStream objectOutputStream, File indexFile, int blockSize) throws IOException {
        int numberOfBlocks = generatedProducts.size() / blockSize;
        int sectionSize = blockSize * PRODUCT_BYTE_SIZE;
        ControlBlock controlBlock = new ControlBlock(HEADER_SIZE, blockSize, numberOfBlocks, sectionSize);
        objectOutputStream.writeObject(controlBlock);
        saveToIndexFile(generatedProducts, indexFile, blockSize);
    }

    private void saveToIndexFile(List<Product> generatedProducts, File indexFile, int blockSize) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(indexFile);
            try (ObjectOutputStream dataOutputStream = new ObjectOutputStream(fileOutputStream)) {
                int blockSizeIndex = (int) Math.sqrt(blockSize);
                ControlBlock indexControlBlock = new ControlBlock(INDEX_HEADER_SIZE, blockSizeIndex, blockSizeIndex, blockSizeIndex * 8);
                dataOutputStream.writeObject(indexControlBlock);
                List<Index> indexList = new ArrayList<>();
                for (int i = 0; i < generatedProducts.size(); i += blockSize) {
                    int id = generatedProducts.get(i).getId();
                    Index index = new Index(id, (INDEX_HEADER_SIZE + (long) i * 8));
                    indexList.add(index);
                    if (indexList.size() == indexControlBlock.getRecordsInBlock()) {
                        dataOutputStream.writeObject(new IndexBlock(indexList));
                        indexList = new ArrayList<>();
                    }
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

    public Product findProductSequential(int key) {
        long position;

        try (RandomAccessFile raf = new RandomAccessFile(indexFile, "r");
             ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(raf.getFD()))) {
            ControlBlock controlBlock = (ControlBlock) objectInputStream.readObject();
            for (int i = 0; i < controlBlock.getNumberOfBlocks(); i++) {
                IndexBlock indexBlock = (IndexBlock) objectInputStream.readObject();
                int previousKey = -1;
                for (Index index : indexBlock.getIndexList()) {
                    int currentKey = index.getBeginningID();
                    if (previousKey != -1 && key >= previousKey && key < currentKey) {
                        return findProductInDataBlock(index.getPosition(), key);
                    }
                    previousKey = currentKey;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private Product findProductInDataBlock(long position, int key) {
        DataBlock dataBlock = readWholeBlock(position);
        List<Product> products = dataBlock.getProductList();
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
            //System.out.println(location);
            DataBlock dataBlock = readWholeBlock(location);
            List<Product> products = dataBlock.getProductList();
            for (Product product : products) {
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

    private DataBlock readWholeBlock(long position) {
        try (RandomAccessFile dataAccessFile = new RandomAccessFile(dataFile, "r");
             ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataAccessFile.getFD()))) {
            dataAccessFile.seek(HEADER_SIZE);
            BlockLogger.writeDataBlockAccessToLog(position, (int) (position / sectionSize)); // FIXME divided by zero
            dataAccessFile.seek(position);
            return (DataBlock) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
