package com.multibank.candle.repository;

import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;

import java.util.List;

public interface CandleRepository {
    void save(String symbol, Timeframe timeframe, Candle candle);
    List<Candle> findBySymbolAndInterval(String symbol, Timeframe timeframe, long from, long to);
}

