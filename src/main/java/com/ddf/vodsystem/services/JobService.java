package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class JobService {
    private LinkedList<Job> jobs = new LinkedList<>();

    public void addJob(Job job) {
        jobs.add(job);
    }

    public Job getNextJob() {
        return jobs.remove();
    }

    public Job getJob(String uuid){
        for (Job job : jobs) {
            if(job.getUuid().equals(uuid)){
                return job;
            }
        }
        throw new RuntimeException("UUID not found");
    }

    @PostConstruct
    public void startProcessingLoop() {
        Thread thread = new Thread(() -> {
            while (true) {
                if (!jobs.isEmpty()) {
                    Runnable task = getNextJob();
                    task.run(); // Execute the task
                }

            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
