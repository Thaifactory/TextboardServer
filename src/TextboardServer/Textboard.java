package TextboardServer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Textboard {
	private List<Message> messageList = null;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	public Textboard() {
		this.messageList = new ArrayList<>();
	}

	public int getSize() {
		return messageList.size();
	}

	public List<Message> getByTime(long time) {
		List<Message> sublist = new ArrayList<>();
		ListIterator<Message> messageIterator = messageList.listIterator();
		readLock.lock();
		try {
			while (messageIterator.hasNext()) {
				Message candidate = messageIterator.next();
				if (candidate.getTime() == time)
					sublist.add(candidate);
			}
			if (!sublist.isEmpty())
				return sublist;
			return null;
		} finally {
			readLock.unlock();
		}
	}

	public List<Message> getByTopic(String topic) {
		List<Message> sublist = new ArrayList<>();
		ListIterator<Message> messageIterator = messageList.listIterator();
		readLock.lock();
		try {
			while (messageIterator.hasNext()) {
				Message candidate = messageIterator.next();
				if (candidate.getTopic() == topic)
					sublist.add(candidate);
			}
			if (!sublist.isEmpty()) {
				return flip(sublist);	//Ausgabe der Liste in invertierter Reihenfolge
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}

	public List<Message> getLastEdited(int index) {
		if (index <= 0) {
			throw new IndexOutOfBoundsException("Number must not be negative or bigger then 0");
		} else if (index >= messageList.size()) {
			return messageList;
		} else {
			int to = messageList.size();
			int from = to - index;
			readLock.lock();
			try {
				return flip(messageList.subList(from, to));	//Ausgabe der Liste in invertierter Reihenfolge
			} finally {
				readLock.unlock();
			}
		}
	}

	public void add(long time, String topic, String message) {
		writeLock.lock();
		try {
			messageList.add(new Message(time, topic, message));
		} finally {
			writeLock.unlock();
		}
	}
	
	private List<Message> flip(List<Message> list) {
		List<Message> flippedList = new ArrayList<>();
		for (int i = list.size() - 1; i >= 0; i--) {
			flippedList.add(list.get(i));
		}
		return flippedList;
	}

	/**
	 * Message Object
	 * @author micha
	 *
	 */
	protected class Message {
		private int numberOfLines;
		private long time;
		private String topic;
		private String message;

		public Message(long time, String topic, String message) {
			this.numberOfLines = countLines(message);
			this.time = time;
			this.topic = topic;
			this.message = message;
		}

		public String toString() {
			return (numberOfLines + "\n" + time + " " + topic + "\n" + message);
		}
		
		public int getNumberOfLines() {
			return numberOfLines;
		}

		public long getTime() {
			return time;
		}

		public String getTopic() {
			return topic;
		}

		public String getMessage() {
			return message;
		}
		
		private int countLines(String message) {
			int count = 1;
			Scanner scanner = new Scanner(message);
			while(scanner.hasNextLine()) {
				scanner.nextLine();
				count++;
			}
			scanner.close();
			return count;
		}
	}
}
