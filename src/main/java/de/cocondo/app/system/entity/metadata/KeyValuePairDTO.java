package de.cocondo.app.system.entity.metadata;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KeyValuePairDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String key;
    private String value;
}
