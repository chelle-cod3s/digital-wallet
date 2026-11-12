package com.digitalwallet.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t from Transaction t
            where t.senderWallet.id = :walletId or t.receiverWallet.id = :walletId
            order by t.createdAt desc, t.id desc
            """)
    List<Transaction> findByWalletIdNewestFirst(@Param("walletId") Long walletId);
}
