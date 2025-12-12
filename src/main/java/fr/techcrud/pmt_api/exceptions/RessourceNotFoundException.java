package fr.techcrud.pmt_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RessourceNotFoundException extends RuntimeException {

    public RessourceNotFoundException() {
        super(); // constructeur sans message
    }

    public RessourceNotFoundException(String message) {
        super(message); // constructeur avec message personnalisé
    }

    private static final long serialVersionUID = 1L;
}
