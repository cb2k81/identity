package de.cocondo.app.system.dto;

import lombok.Data;

import java.io.Serial;

@Data
public class DomainEntityUpdateDTO extends DomainEntityInboundDTO {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;

}
