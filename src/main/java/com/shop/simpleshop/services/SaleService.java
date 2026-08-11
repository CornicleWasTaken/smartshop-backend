package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.*;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.exceptions.ForbiddenActionException;
import com.shop.simpleshop.exceptions.InsufficientStockException;
import com.shop.simpleshop.exceptions.InvalidInputException;
import com.shop.simpleshop.exceptions.SaleItemNotFoundException;
import com.shop.simpleshop.exceptions.SaleNotFoundException;
import com.shop.simpleshop.inventory.TransactionType;
import com.shop.simpleshop.repository.ProductRepository;
import com.shop.simpleshop.repository.SaleRepository;
import com.shop.simpleshop.repository.TransactionVoidRepository;
import com.shop.simpleshop.sales.Sale;
import com.shop.simpleshop.sales.SaleItem;
import com.shop.simpleshop.sales.SaleStatus;
import com.shop.simpleshop.sales.TransactionVoid;
import com.shop.simpleshop.security.Operation;
import com.shop.simpleshop.util.SaleQueryParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SaleService {

    private static final String VOID_REASON = "Void";

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final TransactionVoidRepository transactionVoidRepository;
    private final AuditLogService auditLogService;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       InventoryService inventoryService,
                       TransactionVoidRepository transactionVoidRepository,
                       AuditLogService auditLogService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.transactionVoidRepository = transactionVoidRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public SaleResponseDTO createSale(SaleRequestDTO request) {
        validateNoDuplicateProducts(request.getItems());

        List<Product> products = fetchAndValidateProducts(request.getItems());

        validateStockForAllItems(request.getItems(), products);

        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setCustomerPhone(request.getCustomerPhone());
        sale.setStatus(SaleStatus.COMPLETED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < request.getItems().size(); i++) {
            SaleItemRequestDTO itemRequest = request.getItems().get(i);
            Product product = products.get(i);

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            saleItem.setLineTotal(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            sale.addItem(saleItem);
            totalAmount = totalAmount.add(saleItem.getLineTotal());
        }

        sale.setTotalAmount(totalAmount);

        Sale savedSale = saleRepository.save(sale);

        for (int i = 0; i < request.getItems().size(); i++) {
            SaleItemRequestDTO itemRequest = request.getItems().get(i);
            Product product = products.get(i);
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        return convertToResponseDTO(savedSale);
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        return convertToResponseDTO(sale);
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO getSaleById(Long saleId, SaleQueryParams queryParams) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        return convertToResponseDTO(sale, queryParams);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDTO> getAllSales(Pageable pageable) {
        return saleRepository.findAll(pageable).map(this::convertToResponseDTO);
    }

    /**
     * Voids a single line item of a completed sale: reverses the stock via an
     * {@link TransactionType#IN} inventory transaction, subtracts the line total,
     * and records a {@link TransactionVoid} plus an audit entry. The {@link SaleItem}
     * itself is kept (reports and the sale detail still reference it).
     */
    @Transactional
    public SaleResponseDTO voidItem(Long saleId, Long itemId, String reason) {
        Sale sale = findVoidableSale(saleId);
        SaleItem item = sale.getItems().stream()
                .filter(i -> i.getSaleItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new SaleItemNotFoundException(itemId));

        reverseStock(item);
        sale.setTotalAmount(sale.getTotalAmount().subtract(item.getLineTotal()));
        if (sale.getTotalAmount().signum() < 0) {
            sale.setTotalAmount(BigDecimal.ZERO);
        }
        Sale saved = saleRepository.save(sale);

        recordVoid(saved, item, reason);
        auditLogService.recordCurrentUser(Operation.VOID, "SALE_ITEM", itemId);
        return convertToResponseDTO(saved);
    }

    /**
     * Voids a whole completed sale: reverses stock for every line item, zeroes the
     * total, cancels the sale, and records a single {@link TransactionVoid} (no sale
     * item) plus an audit entry.
     */
    @Transactional
    public SaleResponseDTO voidSale(Long saleId, String reason) {
        Sale sale = findVoidableSale(saleId);

        for (SaleItem item : sale.getItems()) {
            reverseStock(item);
        }
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setTotalAmount(BigDecimal.ZERO);
        Sale saved = saleRepository.save(sale);

        recordVoid(saved, null, reason);
        auditLogService.recordCurrentUser(Operation.VOID, "SALE", saleId);
        return convertToResponseDTO(saved);
    }

    private Sale findVoidableSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new ForbiddenActionException(
                    "Only completed sales can be voided (sale " + saleId + " is " + sale.getStatus() + ")");
        }
        return sale;
    }

    private void reverseStock(SaleItem item) {
        InventoryTransactionRequestDTO reversal = new InventoryTransactionRequestDTO();
        reversal.setProductId(item.getProduct().getProductId());
        reversal.setQuantity(item.getQuantity());
        reversal.setType(TransactionType.IN);
        reversal.setReason(VOID_REASON);
        inventoryService.processTransaction(reversal);
    }

    private void recordVoid(Sale sale, SaleItem item, String reason) {
        TransactionVoid transactionVoid = new TransactionVoid();
        transactionVoid.setSale(sale);
        transactionVoid.setSaleItem(item);
        transactionVoid.setCashier(securityContextName());
        transactionVoid.setReason(reason == null ? VOID_REASON : reason);
        transactionVoid.setOverrideBy(auditLogService.currentOverrideBy());
        transactionVoid.setVoidedAt(LocalDateTime.now());
        transactionVoidRepository.save(transactionVoid);
    }

    private String securityContextName() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private void validateNoDuplicateProducts(List<SaleItemRequestDTO> items) {
        Set<Long> productIds = new HashSet<>();
        for (SaleItemRequestDTO item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new InvalidInputException(
                        "Duplicate product ID found in sale: " + item.getProductId());
            }
        }
    }

    private List<Product> fetchAndValidateProducts(List<SaleItemRequestDTO> items) {
        List<Product> products = new ArrayList<>();
        for (SaleItemRequestDTO item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new InvalidInputException(
                            "Product not found with ID: " + item.getProductId()));
            products.add(product);
        }
        return products;
    }

    private void validateStockForAllItems(List<SaleItemRequestDTO> items, List<Product> products) {
        for (int i = 0; i < items.size(); i++) {
            SaleItemRequestDTO itemRequest = items.get(i);
            Product product = products.get(i);

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName() +
                                "' (SKU: " + product.getSku() + "). " +
                                "Requested: " + itemRequest.getQuantity() +
                                ", Available: " + product.getStockQuantity());
            }
        }
    }

    private SaleResponseDTO convertToResponseDTO(Sale sale) {
        return convertToResponseDTO(sale, null);
    }

    private SaleResponseDTO convertToResponseDTO(Sale sale, SaleQueryParams queryParams) {
        SaleResponseDTO responseDTO = new SaleResponseDTO();

        boolean hasFieldFilter = queryParams != null && queryParams.hasFieldFilter();
        boolean includeItems = queryParams == null || queryParams.isIncludeItems();
        boolean expandProduct = queryParams != null && queryParams.isExpandProduct();

        // Set fields based on filter or include all
        if (!hasFieldFilter || queryParams.shouldIncludeField("saleId")) {
            responseDTO.setSaleId(sale.getSaleId());
        }
        if (!hasFieldFilter || queryParams.shouldIncludeField("saleDate")) {
            responseDTO.setSaleDate(sale.getSaleDate());
        }
        if (!hasFieldFilter || queryParams.shouldIncludeField("totalAmount")) {
            responseDTO.setTotalAmount(sale.getTotalAmount());
        }
        if (!hasFieldFilter || queryParams.shouldIncludeField("customerPhone")) {
            responseDTO.setCustomerPhone(sale.getCustomerPhone());
        }
        if (!hasFieldFilter || queryParams.shouldIncludeField("status")) {
            responseDTO.setStatus(sale.getStatus());
        }

        // Handle items
        if (includeItems) {
            List<SaleItemResponseDTO> itemDTOs = new ArrayList<>();
            for (SaleItem item : sale.getItems()) {
                SaleItemResponseDTO itemDTO = new SaleItemResponseDTO();

                boolean itemHasFieldFilter = queryParams != null && queryParams.getItemFields().isEmpty() == false;

                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("saleItemId")) {
                    itemDTO.setSaleItemId(item.getSaleItemId());
                }
                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("productId")) {
                    itemDTO.setProductId(item.getProduct().getProductId());
                }
                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("productName")) {
                    itemDTO.setProductName(item.getProduct().getName());
                }
                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("quantity")) {
                    itemDTO.setQuantity(item.getQuantity());
                }
                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("unitPrice")) {
                    itemDTO.setUnitPrice(item.getUnitPrice());
                }
                if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("lineTotal")) {
                    itemDTO.setLineTotal(item.getLineTotal());
                }

                // Extended product fields (when expandProduct=true)
                if (expandProduct) {
                    if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("productSku")) {
                        itemDTO.setProductSku(item.getProduct().getSku());
                    }
                    if (!itemHasFieldFilter || queryParams.shouldIncludeItemField("productPrice")) {
                        itemDTO.setProductPrice(BigDecimal.valueOf(item.getProduct().getPrice()));
                    }
                }

                itemDTOs.add(itemDTO);
            }
            responseDTO.setItems(itemDTOs);
        } else {
            responseDTO.setItems(new ArrayList<>());
        }

        return responseDTO;
    }
}
