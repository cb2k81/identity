#!/bin/bash
# bin/fn/log.fn.sh
# Logging functions with configurable LOG_LEVEL (DEBUG, INFO, WARN, ERROR)

# Define numeric log levels
declare -A LOG_LEVELS=(
  [DEBUG]=0
  [INFO]=1
  [WARN]=2
  [ERROR]=3
)

# Default log level if not set
: "${LOG_LEVEL:=INFO}"

# Convert LOG_LEVEL to numeric value, default to INFO if invalid
LOG_LEVEL_NUM=${LOG_LEVELS[${LOG_LEVEL^^}]:-${LOG_LEVELS[INFO]}}

# Internal function to log messages
# Arguments: level_name, message
_log() {
  local level_name="$1"
  shift
  local message="$*"
  local level_num=${LOG_LEVELS[${level_name}]}

  # Only log if level_num >= configured LOG_LEVEL_NUM
  if (( level_num >= LOG_LEVEL_NUM )); then
    local timestamp
    timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
    # stderr for WARN and ERROR
    if [[ "$level_name" == "ERROR" || "$level_name" == "WARN" ]]; then
      echo "[${timestamp}] [${level_name}] ${message}" >&2
    else
      echo "[${timestamp}] [${level_name}] ${message}"
    fi
  fi
}

# Public logging functions
log_debug() { _log DEBUG "$@"; }
log_info()  { _log INFO  "$@"; }
log_warn()  { _log WARN  "$@"; }
log_error() { _log ERROR "$@"; }