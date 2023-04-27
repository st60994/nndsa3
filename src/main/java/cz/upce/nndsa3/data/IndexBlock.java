package cz.upce.nndsa3.data;

import java.io.Serializable;
import java.util.List;

public class IndexBlock implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Index> indexList;

    public IndexBlock(List<Index> indexList) {
        this.indexList = indexList;
    }

    public List<Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
