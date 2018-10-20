package jenu.worker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

final class CountingBufferedInputStream extends BufferedInputStream
{
	private long bytesRead = 0;
	private int line = 0;

	public CountingBufferedInputStream(InputStream in)
	{
		super(in);
	}

	public CountingBufferedInputStream(InputStream in, int size)
	{
		super(in, size);
	}

	public synchronized int read() throws IOException
	{
		int c = super.read();
		if (c != -1)
		{	++bytesRead;
			if (c == '\n')
				++line;
		}
		return c;
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException
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

	public long getBytesRead()
	{	return bytesRead;
	}

	public int getLine()
	{	return line;
	}
}
