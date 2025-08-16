package com.zyneonstudios.nexus.application.main;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code NexusRunner} class is responsible for running tasks at a fixed rate.
 * It uses a {@link ScheduledExecutorService} to schedule the execution of the {@link #run()} method.
 */
public class NexusRunner {

    /**
     * The unique identifier for this runner instance.
     */
    private final UUID runnerID = UUID.randomUUID();

    /**
     * Indicates whether the runner has been started.
     */
    private boolean started = false;

    /**
     * The executor service used to schedule tasks.
     */
    private ScheduledExecutorService executor;

    /**
     * Gets the unique identifier of this runner.
     *
     * @return The UUID of the runner.
     */
    public UUID getRunnerID() {
        return runnerID;
    }

    /**
     * Checks if the runner has been started.
     *
     * @return {@code true} if the runner has been started, {@code false} otherwise.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Gets the executor service used by this runner.
     *
     * @return The {@link ScheduledExecutorService} instance.
     */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Starts the runner, scheduling the {@link #run()} method to be executed at a fixed rate.
     * If the runner is already started, this method does nothing.
     */
    public void start() {
        if (!started) {
            started = true;
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(this::run, 0, 1, TimeUnit.SECONDS);
            NexusApplication.getLogger().log("Started runner with ID: " + runnerID);
        }
    }

    /**
     * The method that is executed periodically by the runner.
     * Currently, it does nothing but can be extended to perform specific tasks.
     */
    private void run() {
        /* TODO background tasks */
    }
}