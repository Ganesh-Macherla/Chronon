# Chronon

> Deterministic event-sourced market replay & debugging engine for algorithmic trading.

Chronon is a Java-based developer tool for replaying historical market sessions and debugging algorithmic trading strategies through time.

Instead of focusing only on backtest performance, Chronon records market updates, strategy decisions, orders, and executions as immutable events so that a session can be deterministically replayed and inspected at any point in time.

## Core Ideas

- Event-sourced architecture
- Deterministic replay
- Virtual clock
- Explainable strategy decisions
- Time-travel debugging
- Simulated execution with slippage and partial fills
- Reconstructable state

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