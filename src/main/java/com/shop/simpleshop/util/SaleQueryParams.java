package com.shop.simpleshop.util;

import com.shop.simpleshop.exceptions.InvalidQueryParameterException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for parsing and validating query parameters for sale endpoints.
 */
public class SaleQueryParams {

    private final boolean includeItems;
    private final boolean expandProduct;
    private final Set<String> fields;
    private final Set<String> itemFields;
    private final boolean pretty;

    private SaleQueryParams(boolean includeItems, boolean expandProduct, Set<String> fields, Set<String> itemFields, boolean pretty) {
        this.includeItems = includeItems;
        this.expandProduct = expandProduct;
        this.fields = fields;
        this.itemFields = itemFields;
        this.pretty = pretty;
    }

    /**
     * Parses and validates query parameters.
     *
     * @param includeItemsStr  the includeItems parameter value (optional, default: true)
     * @param expandProductStr the expandProduct parameter value (optional, default: false)
     * @param fieldsStr        the fields parameter value - comma-separated list (optional)
     * @param prettyStr        the pretty parameter value (optional, default: false)
     * @return a SaleQueryParams instance with parsed values
     * @throws InvalidQueryParameterException if any parameter has an invalid value
     */
    public static SaleQueryParams parse(String includeItemsStr, String expandProductStr, String fieldsStr, String prettyStr) {
        boolean includeItems = parseBoolean(includeItemsStr, "includeItems", true);
        boolean expandProduct = parseBoolean(expandProductStr, "expandProduct", false);
        boolean pretty = parseBoolean(prettyStr, "pretty", false);

        Set<String> fields = null;
        Set<String> itemFields = null;

        if (fieldsStr != null && !fieldsStr.isBlank()) {
            // Use -1 limit to keep empty strings
            String[] parts = fieldsStr.split(",", -1);
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    throw new InvalidQueryParameterException("fields", fieldsStr, "comma-separated list of field names");
                }
                if (trimmed.startsWith("items.")) {
                    if (itemFields == null) {
                        itemFields = new java.util.HashSet<>();
                    }
                    itemFields.add(trimmed.substring(6)); // Remove "items." prefix
                } else {
                    if (fields == null) {
                        fields = new java.util.HashSet<>();
                    }
                    fields.add(trimmed);
                }
            }
        }

        return new SaleQueryParams(includeItems, expandProduct, fields, itemFields, pretty);
    }

    private static boolean parseBoolean(String value, String paramName, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String lower = value.toLowerCase();
        if ("true".equals(lower)) {
            return true;
        }
        if ("false".equals(lower)) {
            return false;
        }

        throw new InvalidQueryParameterException(paramName, value, "boolean (true or false)");
    }

    public boolean isIncludeItems() {
        return includeItems;
    }

    public boolean isExpandProduct() {
        return expandProduct;
    }

    public Set<String> getFields() {
        return fields != null ? Collections.unmodifiableSet(fields) : Collections.emptySet();
    }

    public Set<String> getItemFields() {
        return itemFields != null ? Collections.unmodifiableSet(itemFields) : Collections.emptySet();
    }

    public boolean isPretty() {
        return pretty;
    }

    public boolean hasFieldFilter() {
        return !getFields().isEmpty() || !getItemFields().isEmpty();
    }

    public boolean shouldIncludeField(String fieldName) {
        if (fields == null || fields.isEmpty()) {
            return true; // No filter means include all
        }
        return fields.contains(fieldName);
    }

    public boolean shouldIncludeItemField(String fieldName) {
        if (itemFields == null || itemFields.isEmpty()) {
            return true; // No filter means include all
        }
        return itemFields.contains(fieldName);
    }
}
