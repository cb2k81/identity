#!/bin/bash
# file: jg.fn.sh
# JSON processing functions using centralized logging (log_debug, log_info, log_warn, log_error)

# Assumes log functions are already sourced by wrapper script

# Check if jq is available
checkjq() {
    log_debug "Checking if jq is installed..."

    if ! command -v jq &> /dev/null; then
        log_error "jq is not installed"
        exit 1
    else
        log_debug "jq dependency fulfilled"
    fi
}

# Format and output valid JSON or plain text
# Usage: out <string_or_filename>
out() {
    local input="$1"
    log_debug "Processing input: $input"

    if [[ -f "$input" ]]; then
        # Input is a file
        log_debug "Input is a file: $input"

        if jq . "$input" &> /dev/null; then
            log_debug "File contains valid JSON. Formatting output..."
            jq . "$input"
        else
            log_warn "File does not contain valid JSON. Displaying as plain text..."
            cat "$input"
        fi
    else
        # Input is a string
        log_debug "Input is a string."

        if echo "$input" | jq . &> /dev/null; then
            log_debug "String contains valid JSON. Formatting output..."
            echo "$input" | jq .
        else
            log_warn "String is not valid JSON. Displaying as plain text..."
            echo "$input"
        fi
    fi
}
