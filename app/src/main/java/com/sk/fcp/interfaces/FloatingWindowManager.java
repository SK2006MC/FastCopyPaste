package com.sk.fcp.interfaces;

public interface FloatingWindowManager {
    void startFloatingService();
    void stopFloatingService();
    boolean isServiceRunning();
    void requestOverlayPermission();
    boolean hasOverlayPermission();
} 