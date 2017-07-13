Feature: Redis-Store Logging
  In order that admins can easily obtain runtime information about a Redis-Store session manager,
  an admin can view Redis-Store Session Manager log messages.

  Scenario: Redis-Store Session Manager Logs "About" Information
    Given a redis-store session manager
    When the tomcat instance is started
    Then the redis-store session manager should log "about" information

  Scenario: Redis-Store Session Manager Logs Configured Host
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom host
    When the tomcat instance is started
    Then the redis-store session manager should log a host configuration message

  Scenario: Redis-Store Session Manager Logs Unreachable Host
    Given a redis-store session manager
    And the redis-store session manager is configured with an unreachable host
    When the tomcat instance is started
    And a user starts a session
    Then the redis-store session manager should log a connection failure message

  Scenario: Redis-Store Session Manager Logs Configured Port
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom port
    When the tomcat instance is started
    Then the redis-store session manager should log a port configuration message

  Scenario: Redis-Store Session Manager Logs Misconfigured Port
    Given a redis-store session manager
    And the redis-store session manager is configured with an invalid port
    When the tomcat instance is started
    Then the redis-store session manager should log a port configuration failure message

  Scenario: Redis-Store Session Manager Logs Configured Password
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom password
    When the tomcat instance is started
    Then the redis-store session manager should log a password configuration message

  Scenario: Redis-Store Session Manager Logs Configured Database
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom database
    When the tomcat instance is started
    Then the redis-store session manager should log a database configuration message

  Scenario: Redis-Store Session Manager Logs Misconfigured Database
    Given a redis-store session manager
    And the redis-store session manager is configured with an invalid database
    When the tomcat instance is started
    Then the redis-store session manager should log a database configuration failure message

  Scenario: Redis-Store Session Manager Logs Configured Connection Pool Size
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom connection pool size
    When the tomcat instance is started
    Then the redis-store session manager should log a connection pool size configuration message

  Scenario: Redis-Store Session Manager Logs Misconfigured Connection Pool Size
    Given a redis-store session manager
    And the redis-store session manager is configured with an invalid connection pool size
    When the tomcat instance is started
    Then the redis-store session manager should log a connection pool size configuration failure message

  Scenario: Redis-Store Session Manager Logs Configured Timeout
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom timeout
    When the tomcat instance is started
    Then the redis-store session manager should log a timeout configuration message

  Scenario: Redis-Store Session Manager Logs Misconfigured Timeout
    Given a redis-store session manager
    And the redis-store session manager is configured with an invalid timeout
    When the tomcat instance is started
    Then the redis-store session manager should log a timeout configuration failure message

  Scenario: Redis-Store Session Manager Logs Configured URI
    Given a redis-store session manager
    And the redis-store session manager is configured with a custom URI
    When the tomcat instance is started
    Then the redis-store session manager should log a URI configuration message
