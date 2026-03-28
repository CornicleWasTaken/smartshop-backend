package com.shop.simpleshop.dto;

import com.shop.simpleshop.sales.SaleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SaleResponseDTO {

    private Long saleId;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private String customerPhone;
    private SaleStatus status;
    private List<SaleItemResponseDTO> items;
}
