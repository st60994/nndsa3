package cz.upce.nndsa3.data;

import java.util.ArrayList;
import java.util.List;

public class IndexBlock {
    private List<Index> indexList;

    public IndexBlock(List<Index> indexList) {
        this.indexList = indexList;
    }

    public IndexBlock() {
        this.indexList = new ArrayList<>();
    }

    public List<Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
