package com.sadna_market.market.DomainLayer;

public class StoreExceptions{

    public static class StoreNotFoundException extends RuntimeException {
        public StoreNotFoundException(String message) {
            super(message);
        }
    }

    public static class StoreAlreadyExistsException extends RuntimeException {
        public StoreAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class StoreNotActiveException extends RuntimeException {
        public StoreNotActiveException(String message) {
            super(message);
        }
    }

    public static class StoreAlreadyClosedException extends RuntimeException {
        public StoreAlreadyClosedException(String message) {
            super(message);
        }
    }

    public static class StoreAlreadyOpenException extends RuntimeException {
        public StoreAlreadyOpenException(String message) {
            super(message);
        }
    }

    public static class InvalidStoreDataException extends RuntimeException {
        public InvalidStoreDataException(String message) {
            super(message);
        }
    }

    public static class InsufficientPermissionsException extends RuntimeException {
        public InsufficientPermissionsException(String message) {
            super(message);
        }
    }

    public static class UserNotManagerException extends RuntimeException {
        public UserNotManagerException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class CannotRemoveFounderException extends RuntimeException{
        public CannotRemoveFounderException(String message) {
            super(message);
        }
    }
}


