/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.blockPlacer;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.PluginMain;

/**
 * Operation queue player entry
 *
 * @author SBPrime
 */
public class PlayerEntry {

    /**
     * Maximum job number
     */
    private final int MAX_JOBS = 1024;
    /**
     * Number of samples used in AVG count
     */
    private final int AVG_SAMPLES = 5;
    /**
     * The queue
     */
    private Queue<BlockPlacerEntry> m_queue;
    /**
     * Current block placing speed (blocks per second)
     */
    private double m_speed;
    /**
     * Job id
     */
    private int m_jobId;
    /**
     * List of jobs
     */
    private final HashMap<Integer, BlockPlacerJobEntry> m_jobs;

    public PlayerEntry() {
        m_queue = new ArrayDeque();
        m_speed = 0;
        m_jobId = 0;
        m_jobs = new HashMap<Integer, BlockPlacerJobEntry>();
    }

    public Queue<BlockPlacerEntry> getQueue() {
        return m_queue;
    }

    public void updateQueue(Queue<BlockPlacerEntry> newQueue) {
        m_queue = newQueue;
    }

    public double getSpeed() {
        return m_speed;
    }

    public void updateSpeed(int blocks) {
        m_speed = (m_speed * (AVG_SAMPLES - 1) + blocks) / AVG_SAMPLES;
    }

    public int getNextJobId() {
        int result;
        synchronized (this) {
            result = m_jobId;
            m_jobId = (m_jobId + 1) % MAX_JOBS;
        }
        return result;
    }

    public void addJob(BlockPlacerJobEntry job) {
        synchronized (m_jobs) {
            int id = job.getJobId();
            if (m_jobs.containsKey(id)) {
                m_jobs.remove(id);
            }

            m_jobs.put(id, job);
        }
    }

    public void removeJob(BlockPlacerJobEntry job) {
        synchronized (m_jobs) {
            int id = job.getJobId();
            if (!m_jobs.containsKey(id)) {
                return;
            }
            m_jobs.get(id).cancel();
            m_jobs.remove(id);
        }
    }

    public void removeJob(int jobId) {
        synchronized (m_jobs) {
            if (!m_jobs.containsKey(jobId)) {
                return;
            }
            m_jobs.get(jobId).cancel();
            m_jobs.remove(jobId);
        }
    }

    public void printJobs(Player player) {
        synchronized (m_jobs) {
            if (m_jobs.isEmpty()) {
                return;
            }
            PluginMain.Say(player, ChatColor.YELLOW + "Jobs: ");
            for (BlockPlacerJobEntry job : m_jobs.values()) {
                PluginMain.Say(player, ChatColor.YELLOW + " * " + job.toString()
                        + (job.isStarted() ? (ChatColor.YELLOW + " - " + ChatColor.RED + "running") : ""));
            }
        }
    }
}
