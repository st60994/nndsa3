package cz.upce.nndsa3.data;

import java.io.Serializable;

public class ControlBlock implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer headerSize;
    private Integer recordsInBlock;
    private Integer numberOfBlocks;
    private Integer sectionSize;

    public ControlBlock(Integer headerSize, Integer recordsInBlock, Integer numberOfBlocks, Integer sectionSize) {
        this.headerSize = headerSize;
        this.recordsInBlock = recordsInBlock;
        this.numberOfBlocks = numberOfBlocks;
        this.sectionSize = sectionSize;
    }

    public Integer getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(Integer headerSize) {
        this.headerSize = headerSize;
    }

    public Integer getRecordsInBlock() {
        return recordsInBlock;
    }

    public void setRecordsInBlock(Integer recordsInBlock) {
        this.recordsInBlock = recordsInBlock;
    }

    public Integer getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(Integer numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public Integer getSectionSize() {
        return sectionSize;
    }

    public void setSectionSize(Integer sectionSize) {
        this.sectionSize = sectionSize;
    }
}
