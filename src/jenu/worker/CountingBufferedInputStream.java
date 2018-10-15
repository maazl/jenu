package jenu.worker;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

class CountingBufferedInputStream extends BufferedInputStream
{
	protected long bytesRead = 0;

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
		{
			bytesRead++;
		}
		return c;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		int count = super.read(b, off, len);
		if (count != -1)
			bytesRead += count;
		return count;
	}

	public int read(byte[] b) throws IOException
	{
		int count = super.read(b);
		if (count != -1)
			bytesRead += count;
		return count;
	}

	public long getBytesRead()
	{
		return bytesRead;
	}
}
