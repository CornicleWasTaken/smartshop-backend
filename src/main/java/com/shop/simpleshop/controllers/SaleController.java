package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.SaleRequestDTO;
import com.shop.simpleshop.dto.SaleResponseDTO;
import com.shop.simpleshop.services.SaleService;
import com.shop.simpleshop.util.SaleQueryParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Endpoints for managing sales transactions")
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a new sale")
    public ResponseEntity<SaleResponseDTO> createSale(@Valid @RequestBody SaleRequestDTO request) {
        SaleResponseDTO sale = service.createSale(request);

        return ResponseEntity
                .created(URI.create("/api/sales/" + sale.getSaleId()))
                .body(sale);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get sale by ID",
        description = "Retrieves a sale by its ID with optional query parameters to customize the response"
    )
    public ResponseEntity<SaleResponseDTO> getSaleById(
            @PathVariable("id") Long saleId,
            @Parameter(description = "Include sale items in response (default: true)")
            @RequestParam(value = "includeItems", required = false, defaultValue = "true") String includeItems,
            @Parameter(description = "Expand product details in sale items (default: false)")
            @RequestParam(value = "expandProduct", required = false, defaultValue = "false") String expandProduct,
            @Parameter(description = "Comma-separated list of fields to include (e.g., saleId,totalAmount or items.productName for nested)")
            @RequestParam(value = "fields", required = false) String fields,
            @Parameter(description = "Pretty print JSON response (default: false)")
            @RequestParam(value = "pretty", required = false, defaultValue = "false") String pretty
    ) {
        SaleQueryParams queryParams = SaleQueryParams.parse(includeItems, expandProduct, fields, pretty);
        SaleResponseDTO sale = service.getSaleById(saleId, queryParams);
        return ResponseEntity.ok(sale);
    }

    @GetMapping
    @Operation(summary = "Get all sales with pagination")
    public ResponseEntity<Page<SaleResponseDTO>> getAllSales(Pageable pageable) {
        Page<SaleResponseDTO> sales = service.getAllSales(pageable);
        return ResponseEntity.ok(sales);
    }
}
