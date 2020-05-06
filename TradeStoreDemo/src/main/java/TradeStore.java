import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TradeStore {
    private Map<String, List<Trade>> trades = new ConcurrentHashMap<String, List<Trade>>();

    private void startAutoExpiryTask() {
        Timer autoExpiryTimer = new Timer();
        autoExpiryTimer.schedule(new AutoExpiryTask(), 0, 3600*1000 );
    }

    public TradeStore() {
        startAutoExpiryTask();
    }

    public TradeStore(Map <String, List<Trade>> trades) {
        this.trades = trades;
        startAutoExpiryTask();
    }

    public void addTrade(Trade trade) throws Exception {
        if(null == trade) return;
        //Don't accept trade with Maturity Date < Local Date
        if(trade.getMaturityDate().compareTo(new Date()) < 0) {
            return;
        }
        if(null != trades.get(trade.getTradeId())) {
            List<Trade> localTrades = trades.get(trade.getTradeId());
            if(localTrades.isEmpty()) {
                localTrades.add(trade);
                trades.put(trade.getTradeId(), localTrades);
            } else {
                //Compare version
                Trade currentTrade = localTrades.get(0);
                if(trade.getVersion() < currentTrade.getVersion()) throw new Exception("Trade Version cannot be Lower");
                else if(trade.getVersion() == currentTrade.getVersion()) {
                    localTrades.set(0, trade);
                } else {
                    localTrades.add(0, trade);
                }
            }
        } else {
            List<Trade> localTrades = new ArrayList<Trade>();
            localTrades.add(trade);
            trades.put(trade.getTradeId(), localTrades);
        }
    }

    public List<Trade> getAllTrades() {
        return trades.values().stream().flatMap(e -> e.stream()).collect(Collectors.toList());
    }

    public List<Trade> getTrades(String tradeId) {
        return trades.get(tradeId);
    }

    class AutoExpiryTask extends TimerTask {
        public void run() {
            trades.values().stream().flatMap(e -> e.stream()).filter( t -> t.getMaturityDate().compareTo(new Date()) < 0 ).peek(t -> t.setExpired(true)).collect(Collectors.toList());
        }
    }
}
