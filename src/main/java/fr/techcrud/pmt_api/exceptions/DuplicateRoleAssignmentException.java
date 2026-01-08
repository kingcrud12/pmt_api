package fr.techcrud.pmt_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateRoleAssignmentException extends RuntimeException {

    public DuplicateRoleAssignmentException() {
        super();
    }

    public DuplicateRoleAssignmentException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}
