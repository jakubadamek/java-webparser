package com.jakubadamek.robotemil;

/**
 * Removes diacritics from strings.
 *
 * @author Jakub Adamek
 */
public class DiacriticsRemover
{
	/** Pismena dle Unicode, ze dvoupismen (ij, ae apod.) beru vzdy prvni pismeno
	    Letters with code C0... */
	private static final String nonDiacrC0 =
		"AAAAAAACEEEEII" +
		"IIDNOOOOOxOUUUUYxxaa" +
		"aaaaaceeeeiiiionoooo" +
		"oxouuuuyxyAaAaAaCcCc" +
		"CcCcDdDdEeEeEeEeEeGg" +
		"GgGgGgHhHhIiIiIiIiIi" +
		"IiJjKkkLlLlLlLlLlNnN" +
		"nNnnNnOoOoOoOoRrRrRr" +
		"SsSsSsSsTtTtTtUuUuUu" +
		"UuUuUuWwYyYZzZzZzxxf" +
		"OoUuAaIiOoUuUuUuUuUu" +
		"AaAaOoe";

	/**
	 * @param withDiacritics string with diacritics
	 * @return string without diacritics
	 */
	public static String removeDiacritics(String withDiacritics) {
		if(withDiacritics == null)
			return null;
		StringBuilder retval = new StringBuilder();
		for(int i=0; i < withDiacritics.length(); i ++) {
			char c = withDiacritics.charAt(i);
			if(c >= 0xC0 && c - 0xC0 < nonDiacrC0.length()) {
				retval.append(nonDiacrC0.charAt(c - 0xC0));
			} else {
				retval.append(c);
			}
		}
		return retval.toString();
	}
}
