package cz.upce.nndsa3.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataBlock {
    private List<Product> productList;

    public DataBlock(List<Product> productList) {
        this.productList = productList;
    }

    public DataBlock() {
        this.productList = new ArrayList<>();
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}
