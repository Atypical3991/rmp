package com.biplab.dholey.rmp.util;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.TaskQueueInterface;

import java.util.concurrent.LinkedBlockingQueue;

public class CustomTaskQueue {

    private final String service;

    private final int max_queue_size;
    private final LinkedBlockingQueue<TaskQueueInterface> queue;


    public CustomTaskQueue(String serviceName, int max_queue_size) {
        this.queue = new LinkedBlockingQueue<TaskQueueInterface>();
        this.service = serviceName;
        this.max_queue_size = max_queue_size;
    }

    public boolean pushTask(TaskQueueInterface task) {
        if (queue.size() == max_queue_size) {
            throw new RuntimeException("Queue is full!! please retry after some time.");
        }
        return queue.add(task);
    }

    public TaskQueueInterface popTask() {
        return queue.poll();
    }
}
