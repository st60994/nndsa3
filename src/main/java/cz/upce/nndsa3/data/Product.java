package cz.upce.nndsa3.data;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Comparable<Product> {
    private Integer id;
    private String code;

    public Product(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id) && Objects.equals(code, product.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }


    @Override
    public int compareTo(Product o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", code='" + code + '\'' +
                '}';
    }
}
