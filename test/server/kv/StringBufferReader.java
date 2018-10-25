package server.kv;

import java.io.IOException;
import java.io.Reader;

class StringBufferReader extends Reader {

    private StringBuffer buf;
    private int offset = 0;

    public StringBufferReader(StringBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int off2 = off+offset;
        if (off2 > buf.length() - 1) return -1;
        int srcEnd = off2 + len - 1 > buf.length() - 1 ? buf.length() : off2 + len ;
        buf.getChars(off2, srcEnd, cbuf, 0);
        int read = srcEnd - off2 + 1;
        offset += read;
        return read;
    }

    @Override
    public void close() throws IOException {

    }
}

