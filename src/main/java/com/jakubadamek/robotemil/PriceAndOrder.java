package com.jakubadamek.robotemil;

import java.io.Serializable;

/**
 * Price and order
 */
public class PriceAndOrder implements Serializable {
	private static final long serialVersionUID = 1482674536827012069L;
	/** price */
	Double price;
	/** order on the web site */
	int order;
}