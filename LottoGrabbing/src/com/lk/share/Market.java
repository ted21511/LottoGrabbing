package com.lk.share;

import com.lk.share.Market;

public enum Market {
	
	// HL11x5
	JX("JX", "HL11x5"),
	SD("SD", "HL11x5"),
	GD("GD", "HL11x5"),
	SH("SH", "HL11x5"),
	// LT
	CQ("CQ", "LT"),
	TJ("TJ", "LT"),
	XJ("XJ", "LT"),
	// K3
	JS("JS", "K3"),
	AH("AH", "K3");

    private final String marketName;
    private final String gameCode;

    Market(String marketName, String gameCode) {
        this.marketName = marketName;
        this.gameCode = gameCode;
    }

    public String getMarketName() {
        return marketName;
    }
    public String getGameCode() {
        return gameCode;
    }
    
}
