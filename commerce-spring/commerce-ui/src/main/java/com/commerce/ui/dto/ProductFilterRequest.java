package com.commerce.ui.dto;

public class ProductFilterRequest {

    private String name;
    private Integer minPrice;
    private Integer maxPrice;
    private boolean hasStock;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public boolean isHasStock() { return hasStock; }
    public void setHasStock(boolean hasStock) { this.hasStock = hasStock; }
}
