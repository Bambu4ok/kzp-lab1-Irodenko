package ua.lpnu.kzp;

/**
 * Повідомляє про помилку зберігання, CSV-парсингу або рефлексії.
 */
public class DataStorageException extends Exception {

    /**
     * Створює виняток із повідомленням.
     *
     * @param message опис помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із повідомленням і першопричиною.
     *
     * @param message опис помилки
     * @param cause   першопричина (IOException, ReflectiveOperationException тощо)
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}