package com.queueless.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }

    public TicketNotFoundException(String ticketNumber, boolean isTicketNumber) {
        super("Ticket '" + ticketNumber + "' not found in the virtual queue system.");
    }
}
