package ua.com.nmonokov;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author nmonokov
 * @since 05.04.16
 */
public class CircularQueue<T>  {
    private Queue<T> queue = new ArrayDeque<>();
    private int capacity;
    private int currentElement = 0;

    public CircularQueue(int capacity) {
        this.capacity = capacity;
    }

    public void add(T element) {
        if (currentElement < capacity) {
            queue.add(element);
            currentElement++;
        } else {
            queue.poll();
            queue.add(element);
        }
    }

    public Queue<T> getQueue() {
        return queue;
    }
}
