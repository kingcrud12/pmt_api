package fr.techcrud.pmt_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException() {
        super();
    }

    public PermissionDeniedException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}
