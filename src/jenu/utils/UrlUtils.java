package jenu.utils;

/** Utilities for URLs */
public final class UrlUtils
{
	private UrlUtils()
	{}

	/** Retrieve path component of URL */
	public static String getPath(String url)
	{	int lastSlash = -1;
		for (int i = 0; i < url.length(); ++i)
		{	switch (url.charAt(i))
			{case '/':
				lastSlash = i;
			 //$FALL-THROUGH$
			 default:
				continue;
			 case '?':
			 case '#':
				break;
			}
			break;
		}
		return lastSlash >= 0 ? url.substring(0, lastSlash + 1) : url;
	}
}
