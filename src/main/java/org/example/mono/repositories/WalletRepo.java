package org.example.mono.repositories;

import org.example.mono.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface WalletRepo extends JpaRepository<Wallet,Integer> {
        Wallet findByUser_Id(Integer id);
}

