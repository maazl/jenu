package jenu.worker;

import java.io.InputStream;
import java.io.IOException;

final class CountingInputStream extends InputStream
{
	private final InputStream source;

	private long bytesRead = 0;
	private int line = 0;

	public CountingInputStream(InputStream in)
	{	source = in;
	}

	public void close() throws IOException
	{	source.close();
	}

	public synchronized int read() throws IOException
	{
		int c = source.read();
		if (c != -1)
		{	++bytesRead;
			if (c == '\n')
				++line;
		}
		return c;
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException
	{
		int count = source.read(b, off, len);
		if (count > 0)
		{	bytesRead += count;
			for (int i = off; i < off + len; ++i)
				if (b[i] == '\n')
					++line;
		}
		return count;
	}

	public void skipToEnd() throws IOException
	{	while (skip(65536) > 0) {}
	}

	public long getBytesRead()
	{	return bytesRead;
	}

	public int getLine()
	{	return line;
	}
}
