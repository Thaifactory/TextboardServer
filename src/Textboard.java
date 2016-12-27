import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Textboard {
	private List<Message> messageList;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	
	public Textboard() {
		this.messageList = new ArrayList<>();
	}
	
	public int getSize() {
		return messageList.size();
	}
	
	public Message getByTime(long time) {
		readLock.lock();
		try {
			// To Do
		} finally {
			readLock.unlock();
		}
	}
	
	public Message getByTopic(String topic) {
		readLock.lock();
		try {
			// To Do
		} finally {
			readLock.unlock();
		}
	}
	
	public List<Message> getLastEdited(int index) {
		if(index <= 0) {
			throw new IndexOutOfBoundsException();
		}
		
		int from = 0;
		int to = messageList.size() - 1;
		if(index <= messageList.size()) {
			from = to - index;
		}
		readLock.lock();
		try {
			return messageList.subList(from, to);
		} finally {
			readLock.unlock();
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
	
	
	
	
	
	// Inside class
	private class Message {
		private long time;
		private String topic;
		private String message;
		
		public Message(long time, String topic, String message) {
			this.time = time;
			this.topic = topic;
			this.message = message;
		}
		
		public String toString() {
			return (time + "\n" + topic + "\n" + message);
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

	}
}
