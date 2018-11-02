package jenu.utils;

public final class Statics
{
	private Statics() {}

	public static String strcat(final String s1, final String s2)
	{	if (s1 == null)
			return s2;
		if (s2 == null)
			return s1;
		return s1 + s2;
	}
	public static String strcat(final String s1, final String s2, char delim)
	{	if (s1 == null)
			return s2;
		if (s2 == null)
			return s1;
		return s1 + delim + s2;
	}
}
