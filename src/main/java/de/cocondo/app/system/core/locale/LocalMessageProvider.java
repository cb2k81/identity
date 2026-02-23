package de.cocondo.app.system.core.locale;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocalMessageProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalMessageProvider.class);

    private final MessageSource messageSource;

    private final LocaleResolver localeResolver;

    @Autowired
    public LocalMessageProvider(MessageSource messageSource, LocaleResolver localeResolver) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    private static final Map<Class<? extends Exception>, String> exceptionCodeMap = new HashMap<>();

    // Configure under /resources/messages.properties
    static {
        exceptionCodeMap.put(NoHandlerFoundException.class, "error.not_found");
        exceptionCodeMap.put(HttpRequestMethodNotSupportedException.class, "error.bad_request");
        exceptionCodeMap.put(MethodArgumentNotValidException.class, "error.bad_request");
    }

    public String getExceptionCodeByClass(Exception exception) {
        return exceptionCodeMap.getOrDefault(exception.getClass(), "error.internal_server_error");
    }

    public String getLocalizedErrorMessage(Exception exception, HttpServletRequest request) {
        Locale clientLocale = localeResolver.resolveLocale(request);

        // Try to resolve exception message text
        String errorMessage = exception.getMessage();

        // Check if errorMessage is null
        if (errorMessage == null || errorMessage.isEmpty()) {
            return getMessage("error.internal_server_error", clientLocale);  // Fallback message
        }

        List<String> messageCodes = extractMessageCodes(errorMessage);

        List<String> localizedMessages = getMessages(messageCodes, clientLocale);

        if (!localizedMessages.isEmpty()) {
            // Combine the localized messages into a single string
            return String.join("; ", localizedMessages);
        }

        // If no localized messages were found, return the original error message or fallback
        return getMessage("error.internal_server_error", clientLocale);  // Fallback message
    }


    private List<String> extractMessageCodes(String message) {
        List<String> messageCodes = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{(.+?)\\}").matcher(message);
        while (matcher.find()) {
            messageCodes.add(matcher.group(1));
        }
        return messageCodes;
    }


    public List<String> getMessages(List<String> codes, Locale locale) {
        List<String> messages = new ArrayList<>();

        for (String code : codes) {
            String message = getMessage(code, locale);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }


    public String getMessage(String code, Locale locale) {
        try {
            String message = messageSource.getMessage(code, null, locale);
            logger.debug("Localized message text found, locale="+locale+", code=" + code + ": " + message);
            return message;
        } catch (NoSuchMessageException e) {
            logger.debug("No localized message for locale="+locale+", code=" + code);
            return null; // Return null when no message is found
        }
    }

}
