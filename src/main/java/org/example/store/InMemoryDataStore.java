package org.example.store;

import org.example.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDataStore {

    private final Map<String, BigDecimal> walletBalances = new ConcurrentHashMap<String, BigDecimal>();
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<String, Transaction>();
    private final Map<String, String> receipts = new ConcurrentHashMap<String, String>();

    public Map<String, BigDecimal> getWalletBalances() {
        return walletBalances;
    }

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public Map<String, String> getReceipts() {
        return receipts;
    }
}

