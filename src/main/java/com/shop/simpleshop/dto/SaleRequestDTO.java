package com.shop.simpleshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SaleRequestDTO {

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    private String customerPhone;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private List<SaleItemRequestDTO> items;
}
