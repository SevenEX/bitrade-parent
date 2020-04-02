package cn.ztuo.bitrade.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Zane
 */
@Data
@Builder
public class BrokeInfoDto {
    private String timezone;
    private String serverTime;
    private List<String> brokerFilters;
    private List<Symbol> symbols;
    private List<String> aggregates;
    private List<RateLimit> rateLimits;
    private List<String> options;
    private List<String> contracts;

    public interface Filter {
        String getFilterType();
    }

    @Data
    @Builder
    public static class PriceFilter implements Filter {
        private String minPrice;
        private String maxPrice;
        private String tickSize;
        @Override
        public String getFilterType() {
            return "PRICE_FILTER";
        }
    }

    @Data
    @Builder
    public static class LotSizeFilter implements Filter {
        private String minQty;
        private String maxQty;
        private String stepSize;
        @Override
        public String getFilterType() {
            return "LOT_SIZE";
        }
    }

    @Data
    @Builder
    public static class MinNotionalFilter implements Filter {
        private String minNotional;
        @Override
        public String getFilterType() {
            return "MIN_NOTIONAL";
        }
    }

    @Data
    @Builder
    public static class Symbol {
        private List<Filter> filters;
        private String exchangeId;
        private String symbol;
        private String symbolName;
        private String status;
        private String baseAsset;
        private String baseAssetPrecision;
        private String quoteAsset;
        private String quotePrecision;
        private boolean icebergAllowed;
    }

    @Data
    @Builder
    public static class RateLimit {
        private String rateLimitType;
        private String interval;
        private int intervalUnit;
        private int limit;
    }
}
