package com.shop.simpleshop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SaleItemResponseDTO {

    private Long saleItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Extended product fields (populated when expandProduct=true)
    private String productSku;
    private BigDecimal productPrice;
}
