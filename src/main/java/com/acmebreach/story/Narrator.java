package com.acmebreach.story;

public class Narrator {

    private final boolean fast;
    private long startNanos = System.nanoTime();

    public Narrator(boolean fast) {
        this.fast = fast;
    }

    private void pause(int millis) {
        if (fast) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void scene(String title) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  " + title);
        System.out.println("=".repeat(70));
        pause(300);
    }

    public void line(String text) {
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
        System.out.printf("[t+%6.2fs] %s%n", elapsed, text);
        pause(120);
    }

    public void blank() {
        System.out.println();
    }

    public void raw(String text) {
        System.out.println(text);
    }
}
