package de.cocondo.app.system.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application")
public class ApplicationInfoController {

    private final ApplicationInfoContext applicationInfoContext;

    @Autowired
    public ApplicationInfoController(ApplicationInfoContext applicationInfoContext) {
        this.applicationInfoContext = applicationInfoContext;
    }

    @GetMapping("/info")
    public ApplicationInfoContextDTO getMetadataInfo() {
        return applicationInfoContext.getApplicationInfoDTO();
    }
}
