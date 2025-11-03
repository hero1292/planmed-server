package ru.alexanyan.planmed.export.app.ports;
public interface StoragePort {
    String put(String path, byte[] content, String contentType);
}
