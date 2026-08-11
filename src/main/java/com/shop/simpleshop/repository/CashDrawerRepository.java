package com.shop.simpleshop.repository;

import com.shop.simpleshop.pos.CashDrawerSession;
import com.shop.simpleshop.pos.DrawerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashDrawerRepository extends JpaRepository<CashDrawerSession, Long> {

    Optional<CashDrawerSession> findTopByStatusOrderByOpenedAtDesc(DrawerStatus status);

    Page<CashDrawerSession> findAllBy(Pageable pageable);
}