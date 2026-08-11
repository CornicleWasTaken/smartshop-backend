package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.drawer.CashDrawerSessionResponse;
import com.shop.simpleshop.dto.drawer.DrawerCloseRequest;
import com.shop.simpleshop.dto.drawer.DrawerOpenRequest;
import com.shop.simpleshop.services.CashDrawerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cash-drawer")
@Tag(name = "Cash Drawer", description = "Open/close cash-drawer shifts and list session history")
public class CashDrawerController {

    private final CashDrawerService drawerService;

    public CashDrawerController(CashDrawerService drawerService) {
        this.drawerService = drawerService;
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a cash drawer for the current shift")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or hasAuthority('OVERRIDE')")
    public CashDrawerSessionResponse open(@Valid @RequestBody DrawerOpenRequest request,
                                          Authentication authentication) {
        return drawerService.open(authentication.getName(), request.openingCash());
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a cash drawer, recording the closing float")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public CashDrawerSessionResponse close(@PathVariable Long id,
                                           @Valid @RequestBody DrawerCloseRequest request) {
        return drawerService.close(id, request.closingCash());
    }

    @GetMapping("/sessions")
    @Operation(summary = "List cash-drawer sessions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Page<CashDrawerSessionResponse> sessions(
            @PageableDefault(sort = "openedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return drawerService.sessions(pageable);
    }
}