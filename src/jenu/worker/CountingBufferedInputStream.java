package jenu.worker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

final class CountingBufferedInputStream extends BufferedInputStream
{
	private long bytesRead = 0;
	private int line = 1;

	public CountingBufferedInputStream(InputStream in)
	{
		super(in);
	}

	public CountingBufferedInputStream(InputStream in, int size)
	{
		super(in, size);
	}

	public int read() throws IOException
	{
		int c = super.read();
		if (c != -1)
		{	++bytesRead;
			if (c == '\n')
				++line;
		}
		return c;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		int count = super.read(b, off, len);
		if (count != -1)
		{	bytesRead += count;
			for (int i = off; i < off + len; ++i)
				if (b[i] == '\n')
					++line;
		}
		return count;
	}

	public int read(byte[] b) throws IOException
	{
		int count = super.read(b);
		if (count != -1)
		{	bytesRead += count;
			for (int i = 0; i < b.length; ++i)
				if (b[i] == '\n')
					++line;
		}
		return count;
	}

	public long getBytesRead()
	{	return bytesRead;
	}

	public int getLine()
	{	return line;
	}
}
