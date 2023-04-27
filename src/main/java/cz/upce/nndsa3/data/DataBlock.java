package cz.upce.nndsa3.data;

import java.io.Serializable;
import java.util.List;

public class DataBlock implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Product> productList;

    public DataBlock(List<Product> productList) {
        this.productList = productList;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}
