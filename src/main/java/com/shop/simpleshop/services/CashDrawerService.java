package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.drawer.CashDrawerSessionResponse;
import com.shop.simpleshop.exceptions.DrawerAlreadyOpenException;
import com.shop.simpleshop.pos.CashDrawerSession;
import com.shop.simpleshop.pos.DrawerStatus;
import com.shop.simpleshop.repository.CashDrawerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cash-drawer shifts. Opening records the cashier + float; closing stamps the
 * time and status. Variance (closing - opening) is exposed on the response DTO.
 */
@Service
public class CashDrawerService {

    private final CashDrawerRepository drawerRepository;

    public CashDrawerService(CashDrawerRepository drawerRepository) {
        this.drawerRepository = drawerRepository;
    }

    @Transactional
    public CashDrawerSessionResponse open(String cashier, BigDecimal openingCash) {
        drawerRepository.findTopByStatusOrderByOpenedAtDesc(DrawerStatus.OPEN)
                .ifPresent(open -> {
                    throw new DrawerAlreadyOpenException(
                            "A cash drawer is already open (drawer id " + open.getDrawerId() + ")");
                });

        CashDrawerSession session = new CashDrawerSession();
        session.setCashier(cashier);
        session.setOpeningCash(openingCash);
        session.setOpenedAt(LocalDateTime.now());
        session.setStatus(DrawerStatus.OPEN);
        return CashDrawerSessionResponse.from(drawerRepository.save(session));
    }

    @Transactional
    public CashDrawerSessionResponse close(Long drawerId, BigDecimal closingCash) {
        CashDrawerSession session = drawerRepository.findById(drawerId)
                .orElseThrow(() -> new DrawerAlreadyOpenException(
                        "No cash drawer session found with id " + drawerId));
        session.setClosingCash(closingCash);
        session.setClosedAt(LocalDateTime.now());
        session.setStatus(DrawerStatus.CLOSED);
        return CashDrawerSessionResponse.from(drawerRepository.save(session));
    }

    @Transactional(readOnly = true)
    public Page<CashDrawerSessionResponse> sessions(Pageable pageable) {
        return drawerRepository.findAllBy(pageable).map(CashDrawerSessionResponse::from);
    }
}