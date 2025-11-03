package ru.alexanyan.planmed.common.time;

import java.time.Clock;

public interface ClockProvider {
    Clock clock();
}
