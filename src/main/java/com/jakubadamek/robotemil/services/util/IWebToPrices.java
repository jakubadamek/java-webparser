package com.jakubadamek.robotemil.services.util;

import com.jakubadamek.robotemil.Prices;

public interface IWebToPrices {
	Prices get(String webExcelName);
}
