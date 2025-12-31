package me.jacky.taskMaster.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Stores one team's runtime state: active tasks, completed tasks, score, and win flag.
 *
 * <p>This class intentionally does NOT know anything about Bukkit / config. It only
 * manages state. New tasks are produced by the supplied {@link Supplier}.</p>
 */
public final class TeamState {

    private final List<String> activeTasks = new ArrayList<>();
    private final List<String> completedTasks = new ArrayList<>();
    private final Supplier<String> taskSupplier;

    private int score = 0;
    private boolean hasWon = false;

    /**
     * @param tasksPerTeam number of active tasks to keep
     * @param taskSupplier supplier that returns a new task key string (e.g. "BLOCK_BREAK:STONE")
     */
    public TeamState(final int tasksPerTeam, final Supplier<String> taskSupplier) {
        this.taskSupplier = taskSupplier;
        for (int i = 0; i < tasksPerTeam; i++) {
            refreshOneTask();
        }
    }

    public List<String> getActiveTasks() {
        return activeTasks;
    }

    public List<String> getCompletedTasks() {
        return completedTasks;
    }

    public int getScore() {
        return score;
    }

    public void addScore(final int points) {
        score += points;
    }

    public boolean hasWon() {
        return hasWon;
    }

    public void setHasWon(final boolean won) {
        this.hasWon = won;
    }

    /** Replace the task at index with a freshly generated one. */
    public void refreshTask(final int taskIndex) {
        if (taskIndex >= 0 && taskIndex < activeTasks.size()) {
            String oldTask = activeTasks.remove(taskIndex);
            String newTask = taskSupplier.get();
            activeTasks.add(taskIndex, newTask);
            System.out.println("刷新任务: " + oldTask + " -> " + newTask);
        }
    }

    /** Append a new task to the active task list. */
    private void refreshOneTask() {
        activeTasks.add(taskSupplier.get());
    }

    /**
     * Mark the given task as complete and immediately refresh it.
     *
     * @return true if the task existed in activeTasks, false otherwise
     */
    public boolean completeTask(final String task, final int points) {
        int taskIndex = -1;
        for (int i = 0; i < activeTasks.size(); i++) {
            if (activeTasks.get(i).equals(task)) {
                taskIndex = i;
                break;
            }
        }

        if (taskIndex != -1) {
            completedTasks.add(activeTasks.get(taskIndex));
            refreshTask(taskIndex);
            return true;
        }
        return false;
    }
}
