# Complexity Notes

## Wallet operations
- `addMoney`, `debit`, `credit`, `getBalance`
- App-level complexity: O(1)
- Persistence: single-row lookup/update, effectively O(1) by primary key index.

## Orchestrated payment operations
- `payMerchant`, `payElectricityBill`, `rechargeMobile`
- App-level complexity: O(1) per request path.
- Steps: wallet debit, external call, optional compensation, transaction persist.
- DB operations per request: bounded constant number of writes/reads.

## Why this matters
- Predictable request-time behavior under increasing load.
- Easier capacity planning and lower tail-latency variance.

## Optimization direction
- Add batching only for offline jobs, not online payment paths.
- Use async/event-driven processing for slow external systems.
- Add caching only for read-heavy metadata (not mutable balances).

