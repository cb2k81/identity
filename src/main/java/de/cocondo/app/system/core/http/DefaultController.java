package de.cocondo.app.system.core.http;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DefaultController {

    private final ResourceLoader resourceLoader;

    public DefaultController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public Resource getFavicon() {
        return resourceLoader.getResource("classpath:/static/favicon.ico");
    }

    @GetMapping("/index.html")
    @ResponseBody
    public Resource getIndex() {
        return resourceLoader.getResource("classpath:/static/index.html");
    }

    @GetMapping("/")
    @ResponseBody
    public Resource getRoot() {
        return resourceLoader.getResource("classpath:/static/index.html");
    }


    @RequestMapping("/*")
    public String handleDefault() {
        throw new InvalidConfigurationPropertyValueException("Invalid path", "path", "Unknown path");
    }
}
