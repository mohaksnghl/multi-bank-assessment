# Candle Aggregation Service

Spring Boot service that processes bid/ask market data and converts it to OHLC candlesticks. Provides a REST API to retrieve historical candle data.

## Overview

Takes continuous bid/ask events and aggregates them into candles. Supports multiple symbols and timeframes (1s, 5s, 1m, 15m, 1h). Uses mid-price calculation (bid + ask) / 2 for OHLC values.

## Design Decisions

- Mid-price calculation: `(bid + ask) / 2` for OHLC
- Time alignment: Candles aligned to interval boundaries
- Volume: Count of events (ticks) in the period
- Storage: In-memory ConcurrentHashMap (can be extended to database)
- Thread-safety: Concurrent collections with locks

## Assumptions

- Events have Unix timestamps in seconds
- Bid <= ask (invalid events are filtered)
- Volume is tick count, not actual trade volume
- Events arrive in roughly chronological order (out-of-order events may be placed in wrong candles)

### Trade-offs Made

1. **In-Memory Storage**
   - **Trade-off**: Fast access but data is lost on restart
   - **Rationale**: Simpler for assessment, can be extended to persistent storage

2. **Mid-Price for OHLC**
   - **Trade-off**: Uses (bid + ask) / 2 instead of separate bid/ask candles
   - **Rationale**: Standard approach for market data aggregation, simplifies implementation

3. **Event-Driven Flush**
   - **Trade-off**: Candles flush when new events arrive for different time windows
   - **Rationale**: Ensures data consistency, but current active candle may not be queryable until next window starts

4. **Per-Candle Locks**
   - **Trade-off**: Fine-grained locking per candle key for thread safety
   - **Rationale**: Allows concurrent processing but adds lock management overhead

5. **Synchronous Processing Per Timeframe**
   - **Trade-off**: Each event is processed for all timeframes sequentially
   - **Rationale**: Simpler logic, but could be parallelized for better performance

## Building and Running

Requirements: Java 17+, Maven 3.6+

Build:
```bash
mvn clean install
```

Run:
```bash
mvn spring-boot:run
```

Service runs on port 8080 by default.

Configuration in `application.properties`:
- `market.generator.enabled`: Enable/disable generator
- `market.generator.rate-per-second`: Events per second
- `market.generator.symbols`: Comma-separated symbols

## API

### GET /history

Get historical candles for a symbol and timeframe.

Params:
- `symbol`: Trading symbol (e.g., BTC-USD)
- `interval`: Timeframe (1s, 5s, 1m, 15m, 1h)
- `from`: Start timestamp (Unix seconds)
- `to`: End timestamp (Unix seconds)

Example:
```
GET /history?symbol=BTC-USD&interval=1m&from=1620000000&to=1620000600
```

Response:
```json
{
  "s": "ok",
  "t": [1620000000, 1620000060, 1620000120],
  "o": [29500.5, 29501.0, 29502.5],
  "h": [29510.0, 29505.0, 29508.0],
  "l": [29490.0, 29500.0, 29501.0],
  "c": [29505.0, 29502.0, 29506.0],
  "v": [10, 8, 12]
}
```

### GET /health

Health check endpoint.

## Testing

Run tests:
```bash
mvn test
```

Tests cover:
- Aggregation service (single/multiple events, timeframes)
- API controller (validation, error handling)
- Repository (save/retrieve, range queries)

## Usage

After starting, wait a few seconds for data generation, then:

```bash
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=1620000000&to=1620000600"
curl "http://localhost:8080/health"
```

## Manual Testing

Test with current timestamps:

# Get last 10 minutes of data
CURRENT=$(date +%s)
FROM=$((CURRENT - 600))
curl "http://localhost:8080/history?symbol=BTC-USD&interval=5s&from=${FROM}&to=${CURRENT}"This queries the last 10 minutes of 5-second candles for BTC-USD using current timestamps.

## Bonus Features

1. **Extensible Event System**: Interfaces for `EventPublisher`/`EventConsumer` (Spring Events, Kafka-ready)
2. **Extensible Storage**: Interfaces for `ActiveCandleStore`/`LockManager` (Memory, Redis/DynamoDB-ready)
3. **Orchestrator Pattern**: Controller delegates to service layer
4. **Configuration-Driven**: All settings via `application.properties`

## Future Improvements

- Database persistence (PostgreSQL/TimescaleDB)
- WebSocket for real-time updates
- Kafka integration
- Metrics and monitoring

