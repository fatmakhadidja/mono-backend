package org.example.mono.repositories;
import org.example.mono.models.Transaction;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction,Integer> {


}
