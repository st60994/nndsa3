package cz.upce.nndsa3.structure;

import cz.upce.nndsa3.data.*;
import cz.upce.nndsa3.util.BlockLogger;
import cz.upce.nndsa3.util.ByteArrayDeserializer;
import cz.upce.nndsa3.util.ProductGenerator;

import java.io.*;
import java.util.*;

import static cz.upce.nndsa3.util.ProductGenerator.MAX_STRING_LENGTH;
import static cz.upce.nndsa3.util.ProductGenerator.PRODUCT_BYTE_SIZE;

public class IndexSequentialFileBuilder {

    private static final int HEADER_SIZE = 16;
    private static final int INDEX_BYTE_SIZE = 12;
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
                dataOutputStream.writeInt(HEADER_SIZE);
                dataOutputStream.writeInt((int) Math.sqrt(numberOfRecords));
                dataOutputStream.writeInt((int) Math.sqrt(numberOfRecords));
                dataOutputStream.writeInt(INDEX_BYTE_SIZE * (int) Math.sqrt(numberOfRecords));
                for (int i = 0; i < generatedProducts.size(); i += blockSize) {
                    int id = generatedProducts.get(i).getId();
                    dataOutputStream.writeInt(generatedProducts.get(i).getId());
                    dataOutputStream.writeLong(HEADER_SIZE + (long) i * PRODUCT_BYTE_SIZE);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Product findProduct(int key) {
        try (RandomAccessFile raf = new RandomAccessFile(indexFile, "r")) {
            byte[] buffer = new byte[HEADER_SIZE];
            raf.read(buffer, 0, HEADER_SIZE);
            BlockLogger.writeAccessToIndexControlBlock();
            ControlBlock controlBlock = ByteArrayDeserializer.getControlBlockFromByteArray(buffer);
            for (int i = 0; i < controlBlock.getNumberOfBlocks(); i++) {
                buffer = new byte[controlBlock.getSectionSize()];
                raf.read(buffer, 0, buffer.length);
                BlockLogger.writeIndexBlockAccessToLog(HEADER_SIZE + controlBlock.getSectionSize() * i, i);
                IndexBlock indexBlock = ByteArrayDeserializer.getIndexBlockFromByteArray(buffer);
                Index previousIndex = null;
                for (Index index : indexBlock.getIndexList()) {
                    int currentKey = index.getBeginningID();
                    if (previousIndex != null && key >= previousIndex.getBeginningID() && key < currentKey) {
                        return findProductInDataBlock(previousIndex.getPosition(), key);
                    }
                    previousIndex = index;
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
        try (RandomAccessFile raf = new RandomAccessFile(indexFile, "r")) {
            byte[] buffer = new byte[HEADER_SIZE];
            raf.read(buffer, 0, HEADER_SIZE);
            BlockLogger.writeAccessToIndexControlBlock();
            ControlBlock controlBlock = ByteArrayDeserializer.getControlBlockFromByteArray(buffer);
            ControlBlock dataControlBlock;
            try (RandomAccessFile dataAccessFile = new RandomAccessFile(dataFile, "r")) {
                buffer = new byte[HEADER_SIZE];
                raf.read(buffer, 0, HEADER_SIZE);
                dataAccessFile.read(buffer, 0, HEADER_SIZE);
                BlockLogger.writeAccessToDataControlBlock();
                dataControlBlock = ByteArrayDeserializer.getControlBlockFromByteArray(buffer);
            }

            for (int i = 0; i < controlBlock.getNumberOfBlocks(); i++) {
                raf.seek(HEADER_SIZE + (long) i * controlBlock.getSectionSize());
                buffer = new byte[controlBlock.getSectionSize()];
                raf.read(buffer, 0, buffer.length);
                BlockLogger.writeIndexBlockAccessToLog(HEADER_SIZE + (long) i * controlBlock.getSectionSize(), i);
                IndexBlock indexBlock = ByteArrayDeserializer.getIndexBlockFromByteArray(buffer);
                for (Index index : indexBlock.getIndexList()) {
                    try (RandomAccessFile dataAccessFile = new RandomAccessFile(dataFile, "r")) {
                        dataAccessFile.seek(index.getPosition());
                        buffer = new byte[dataControlBlock.getSectionSize()];
                        dataAccessFile.read(buffer, 0, buffer.length);
                        BlockLogger.writeDataBlockAccessToLog(index.getPosition(), index.getPosition().intValue() / dataControlBlock.getSectionSize());
                        DataBlock dataBlock = ByteArrayDeserializer.getDataBlockFromByteArray(buffer);
                        dataBlock.getProductList().forEach((product -> keysList.add(product.getId())));
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        try (RandomAccessFile dataAccessFile = new RandomAccessFile(dataFile, "r")) {
            byte[] buffer = new byte[HEADER_SIZE];
            dataAccessFile.read(buffer, 0, HEADER_SIZE);
            BlockLogger.writeAccessToDataControlBlock();
            ControlBlock controlBlock = ByteArrayDeserializer.getControlBlockFromByteArray(buffer);
            dataAccessFile.seek(position);
            buffer = new byte[controlBlock.getSectionSize()];
            dataAccessFile.read(buffer, 0, buffer.length);
            BlockLogger.writeDataBlockAccessToLog(position, (int) position / controlBlock.getSectionSize());
            return ByteArrayDeserializer.getDataBlockFromByteArray(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
