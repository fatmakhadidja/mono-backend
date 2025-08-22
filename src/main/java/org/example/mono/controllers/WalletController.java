package org.example.mono.controllers;


import jakarta.transaction.Transactional;
import org.example.mono.models.Transaction;
import org.example.mono.models.User;
import org.example.mono.models.Wallet;
import org.example.mono.repositories.TransactionRepo;
import org.example.mono.repositories.UserRepo;
import org.example.mono.repositories.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @GetMapping("/getWalletInfo")
    public ResponseEntity<Wallet> getWalletInfo() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            // Get the user entity from the email
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get the wallet linked to this user
            Wallet wallet = walletRepo.findByUser_Id(user.getId());

            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/add_transaction")
    public ResponseEntity<?> addTransaction(@RequestBody Transaction transaction){
        try{
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Wallet wallet;
                wallet = ((User) principal).getWallet();
            transaction.setWallet(wallet);
            if(transaction.isIncome()){
                wallet.setBalance(wallet.getBalance()+transaction.getAmount());
            } else {
                wallet.setBalance(wallet.getBalance()-transaction.getAmount());
            }

            walletRepo.save(wallet);
            transactionRepo.save(transaction);

            return ResponseEntity.ok(transaction);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @Transactional
    @DeleteMapping("/delete_transaction/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer id) {
        try {
            // Get authenticated user properly
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = ((UserDetails) principal).getUsername();
            User user = userRepo.findByEmail(email).orElseThrow();
            Wallet wallet = walletRepo.findByUser_Id(user.getId());

            // Fetch transaction
            Transaction transaction = transactionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // Ensure ownership
            if (!transaction.getWallet().getId().equals(wallet.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You cannot delete a transaction that is not yours");
            }

            // Update wallet balance
            if (transaction.isIncome()) {
                wallet.setBalance(wallet.getBalance() - transaction.getAmount());
            } else {
                wallet.setBalance(wallet.getBalance() + transaction.getAmount());
            }

            wallet.getTransactions().remove(transaction);
            transaction.setWallet(null);
            walletRepo.save(wallet);
            transactionRepo.flush();

            return ResponseEntity.ok("Transaction deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }



}
