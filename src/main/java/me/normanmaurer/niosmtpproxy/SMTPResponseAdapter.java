package me.normanmaurer.niosmtpproxy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.apache.james.protocols.api.Response;

public class SMTPResponseAdapter implements Response{

	private final SMTPResponse response;
	private boolean endSession;

	public SMTPResponseAdapter(SMTPResponse response, boolean endSession) {
		this.response = response;
		this.endSession = endSession;
	}
	
	@Override
	public String getRetCode() {
		return Integer.toString(response.getCode());
	}

	@Override
	public List<CharSequence> getLines() {
		return new LinesWrapper(response.getLines());
	}

	@Override
	public boolean isEndSession() {
		return endSession;
	}
	
	private final class LinesWrapper implements List<CharSequence> {

		private final List<String> lines;
		
		public LinesWrapper(List<String> lines) {
			this.lines = lines;
		}
		
		@Override
		public int size() {
			return lines.size();
		}

		@Override
		public boolean isEmpty() {
			return lines.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return lines.contains(o);
		}

		@Override
		public Iterator<CharSequence> iterator() {
			return null;
		}

		@Override
		public Object[] toArray() {
			return lines.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return lines.toArray(a);
		}

		@Override
		public boolean add(CharSequence e) {
			throw new UnsupportedOperationException("Read-Only");
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Read-Only");

		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return lines.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends CharSequence> c) {
			throw new UnsupportedOperationException("Read-Only");

		}

		@Override
		public boolean addAll(int index, Collection<? extends CharSequence> c) {
			throw new UnsupportedOperationException("Read-Only");

		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("Read-Only");

		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("Read-Only");

		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("Read-Only");
			
		}

		@Override
		public CharSequence get(int index) {
			return lines.get(index);
		}

		@Override
		public CharSequence set(int index, CharSequence element) {
			throw new UnsupportedOperationException("Read-Only");
		}

		@Override
		public void add(int index, CharSequence element) {
			throw new UnsupportedOperationException("Read-Only");			
		}

		@Override
		public CharSequence remove(int index) {
			throw new UnsupportedOperationException("Read-Only");
		}

		@Override
		public int indexOf(Object o) {
			return lines.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o) {
			return lines.lastIndexOf(o);
		}

		@Override
		public ListIterator<CharSequence> listIterator() {
			return new LinesWrapperListIterator(lines.listIterator());
		}

		@Override
		public ListIterator<CharSequence> listIterator(int index) {
			return new LinesWrapperListIterator(lines.listIterator(index));
		}

		@Override
		public List<CharSequence> subList(int fromIndex, int toIndex) {
			return new LinesWrapper(lines.subList(fromIndex, toIndex));
		}
		
		
		
		
		
	}
	
	private final class LinesWrapperListIterator implements ListIterator<CharSequence> {

		private final ListIterator<String> linesIt;

		public LinesWrapperListIterator(ListIterator<String> linesIt) {
			this.linesIt = linesIt;
		}
		
		@Override
		public boolean hasNext() {
			return linesIt.hasNext();
		}

		@Override
		public CharSequence next() {
			return linesIt.next();
		}

		@Override
		public boolean hasPrevious() {
			return linesIt.hasPrevious();
		}

		@Override
		public CharSequence previous() {
			return linesIt.previous();
		}

		@Override
		public int nextIndex() {
			return linesIt.nextIndex();
		}

		@Override
		public int previousIndex() {
			return linesIt.previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Read-Only");
		}

		@Override
		public void set(CharSequence e) {
			throw new UnsupportedOperationException("Read-Only");
			
		}

		@Override
		public void add(CharSequence e) {
			throw new UnsupportedOperationException("Read-Only");
			
		}
		
	}

}
