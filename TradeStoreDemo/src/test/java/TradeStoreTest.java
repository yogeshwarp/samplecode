import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TradeStoreTest {
    static TradeStore tradeStore = null;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @BeforeAll
    static void testSetup() {
        tradeStore = new TradeStore();
        try {
            tradeStore.addTrade(new Trade("T1", 1, "CP1", "B1", dateFormat.parse("2020-07-08"), false));
            tradeStore.addTrade(new Trade("T1", 2, "CP1", "B1", dateFormat.parse("2020-07-06"), false));
            tradeStore.addTrade(new Trade("T2", 1, "CP2", "B2", dateFormat.parse("2020-07-10"), false));
            tradeStore.addTrade(new Trade("T3", 1, "CP3", "B3", dateFormat.parse("2020-09-24"), false));
            tradeStore.addTrade(new Trade("T4", 1, "CP4", "B4", dateFormat.parse("2020-06-08"), false));
            tradeStore.addTrade(new Trade("T4", 2, "CP4", "B4", dateFormat.parse("2020-06-09"), false));
            tradeStore.addTrade(new Trade("T4", 3, "CP4", "B4", dateFormat.parse("2020-06-10"), false));
            tradeStore.addTrade(new Trade("T4", 4, "CP4", "B4", dateFormat.parse("2020-06-27"), false));
            tradeStore.addTrade(new Trade("T5", 1, "CP5", "B5", dateFormat.parse("2020-07-15"), false));
            tradeStore.addTrade(new Trade("T6", 1, "CP6", "B6", dateFormat.parse("2020-04-08"), false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Invalid Version No")
    void invalidVersionTest(TestInfo test) throws Exception {
        assertThrows(Exception.class, () -> tradeStore.addTrade(new Trade("T1", 1, "CP1", "B1", dateFormat.parse("2020-07-08"), false)));
    }

    @Test
    @DisplayName("Update Version No")
    void updateVersionTest(TestInfo testInfo) throws Exception {
        Trade updateTrade = new Trade("T4", 4, "CP4-New", "B4-New", dateFormat.parse("2020-06-29"), false);
        tradeStore.addTrade(updateTrade);
        Trade postUpdateTrade = tradeStore.getTrades(updateTrade.getTradeId()).get(0);
        assertTrue(postUpdateTrade.getCounterPartyId().equals(updateTrade.getCounterPartyId()));
    }

    @Test
    @DisplayName("Invalid Maturity Date")
    void invalidMaturityTest(TestInfo testInfo) throws Exception {
        tradeStore.addTrade(new Trade("T100", 1, "CP1", "B1", dateFormat.parse("2020-05-05"), false));
        assertTrue(null == tradeStore.getTrades("T100") || tradeStore.getTrades("T100").isEmpty());
    }

    @Test
    @DisplayName("Auto Expiry Test for all Trade Data")
    void autoExpiryForAllTradeDataTest(TestInfo testInfo) throws Exception {
        Map<String , List<Trade>> trades = new ConcurrentHashMap<String, List<Trade>>();
        List<Trade> localTrades = new ArrayList<Trade>();
        localTrades.add(new Trade("T100", 1, "CP1", "B1", dateFormat.parse("2020-05-05"), false));
        trades.put("T100", localTrades);
        TradeStore tradeStore = new TradeStore(trades);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(tradeStore.getTrades("T100").get(0).getExpired());
    }
}