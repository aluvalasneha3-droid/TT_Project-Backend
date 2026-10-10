package com.example.TT.Backend.repository;

import com.example.TT.Backend.model.Holding;
import com.example.TT.Backend.model.Stock;
import com.example.TT.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUser(User user);
    Optional<Holding> findByUserAndStock(User user, Stock stock);
}
