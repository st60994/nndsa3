package cz.upce.nndsa3.util;

import cz.upce.nndsa3.data.Product;

import java.util.*;

public class ProductGenerator {
    private static final Random random = new Random();
    public static final int MAX_STRING_LENGTH = 16;
    private static final int MIN_STRING_LENGTH = 3;
    private static final int MAX_ID = 100000;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int PRODUCT_BYTE_SIZE = MAX_STRING_LENGTH * 2 + 4; // java char = 2 bytes

    public static List<Product> generateProducts(int numberOfProducts) {
        List<Product> generatedProducts = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < numberOfProducts; i++) {
            int id = random.nextInt(MAX_ID);
            while (ids.contains(id)) {
                id = random.nextInt(MAX_ID);
            }
            int randomStringLength = random.nextInt((MAX_STRING_LENGTH - MIN_STRING_LENGTH) + 1) + MIN_STRING_LENGTH;
            StringBuilder codeBuilder = new StringBuilder();
            for (int j = 0; j < randomStringLength; j++) {
                int randomAlphabetIndex = random.nextInt(ALPHABET.length());
                codeBuilder.append(ALPHABET.charAt(randomAlphabetIndex));
            }
            Product product = new Product(id, codeBuilder.toString());
            generatedProducts.add(product);
            ids.add(id);
        }
        return generatedProducts;
    }
}
