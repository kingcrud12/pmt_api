package fr.techcrud.pmt_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException() {
        super();
    }

    public PermissionNotFoundException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}
