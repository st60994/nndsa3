package cz.upce.nndsa3.data;

public class ControlBlock {

    private Integer headerSize;
    private Integer recordsInBlock;
    private Integer numberOfBlocks;
    private Integer sectionSize;

    public ControlBlock(Integer recordsInBlock, Integer numberOfBlocks, Integer sectionSize) {
        this.recordsInBlock = recordsInBlock;
        this.numberOfBlocks = numberOfBlocks;
        this.sectionSize = sectionSize;
    }
    public ControlBlock(){
        this.recordsInBlock = 0;
        this.numberOfBlocks = 0;
        this.sectionSize = 0;
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
