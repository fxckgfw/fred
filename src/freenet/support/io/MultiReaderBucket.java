/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package freenet.support.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import freenet.support.api.Bucket;

/**
 * A wrapper for a read-only bucket providing for multiple readers. The data is 
 * only freed when all of the readers have freed it.
 * @author toad
 */
public class MultiReaderBucket {
	
	private final Bucket bucket;
	
	// Assume there will be relatively few readers
	private ArrayList readers;
	
	private boolean closed;
	
	public MultiReaderBucket(Bucket underlying) {
		bucket = underlying;
	}

	/** Get a reader bucket */
	public Bucket getReaderBucket() {
		synchronized(this) {
			if(closed) return null;
			Bucket d = new ReaderBucket();
			if(readers == null) readers = new ArrayList();
			readers.add(d);
			return d;
		}
	}

	class ReaderBucket implements Bucket {

		public void free() {
			synchronized(MultiReaderBucket.this) {
				readers.remove(this);
				if(!readers.isEmpty()) return;
				readers = null;
				if(closed) return;
				closed = true;
			}
			bucket.free();
		}

		public InputStream getInputStream() throws IOException {
			return bucket.getInputStream();
		}

		public String getName() {
			return bucket.getName();
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Read only");
		}

		public boolean isReadOnly() {
			return true;
		}

		public void setReadOnly() {
			// Already read only
		}

		public long size() {
			return bucket.size();
		}
		
		public void finalize() {
			free();
		}
		
	}
	
}
