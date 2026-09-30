# Chronon

> Deterministic event-sourced market replay & debugging engine for algorithmic trading.

Chronon is a Java-based developer tool for replaying historical market sessions and debugging algorithmic trading systems through time.

Instead of treating a trading failure as a final outcome, Chronon preserves the events that produced it. Market updates, strategy decisions, orders, and executions, so engineers can reconstruct what happened and reproduce the same state deterministically.

## Core Ideas

* Event-sourced architecture
* Deterministic replay
* Virtual clock
* Immutable event history
* Time-travel debugging
* Explainable strategy decisions
* Simulated execution with slippage and partial fills
* Reconstructable state

## Architecture

```text
Historical Market Data
          |
          v
    Virtual Clock
          |
          v
     Event Store
          |
          v
      Event Bus
      /   |   \
     /    |    \
Strategy Matching Metrics
 Engine   Engine  Collector
          |
          v
    Replay / Debugger
```

Chronon focuses on **behavioral truth**: what happened, when it happened, and how the system arrived at a particular state.

It is designed to work alongside **Concord**, which focuses on cross-system reconciliation and consistency.
