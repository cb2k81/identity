package de.cocondo.app.system.info;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (anonymous) application info endpoint.
 * Used for /public vs /api path-security integration tests.
 */
@RestController
@RequestMapping("/public/application")
public class PublicApplicationInfoController {

    private final ApplicationInfoContext applicationInfoContext;

    public PublicApplicationInfoController(ApplicationInfoContext applicationInfoContext) {
        this.applicationInfoContext = applicationInfoContext;
    }

    @GetMapping("/info")
    public ApplicationInfoContextDTO getMetadataInfo() {
        return applicationInfoContext.getApplicationInfoDTO();
    }
}