package cz.upce.nndsa3.util;

import cz.upce.nndsa3.data.*;

import java.nio.ByteBuffer;

public class ByteArrayDeserializer {
    public static ControlBlock getControlBlockFromByteArray(byte[] bytes) {
        ControlBlock controlBlock = new ControlBlock();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        controlBlock.setHeaderSize(buffer.getInt());
        controlBlock.setRecordsInBlock(buffer.getInt());
        controlBlock.setNumberOfBlocks(buffer.getInt());
        controlBlock.setSectionSize(buffer.getInt());
        return controlBlock;
    }

    public static IndexBlock getIndexBlockFromByteArray(byte[] bytes) {
        IndexBlock indexBlock = new IndexBlock();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.hasRemaining()) {
            int beginningId = buffer.getInt();
            long position = buffer.getLong();
            indexBlock.getIndexList().add(new Index(beginningId, position));
        }
        return indexBlock;
    }

    public static DataBlock getDataBlockFromByteArray(byte[] bytes) {
        DataBlock dataBlock = new DataBlock();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) {
            int productId = buffer.getInt();
            StringBuilder productCode = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                productCode.append(buffer.getChar());
            }
            dataBlock.getProductList().add(new Product(productId, productCode.toString()));
        }
        return dataBlock;
    }
}
