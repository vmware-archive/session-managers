Feature: Redis-Store Session Manager Configuration
  In order that admins can use the Redis-Store Session Manager with various Redis configurations,
  an admin can configure Redis-Store Session Manager Redis attributes

  Scenario: Redis-Store Session Manager Connects to Custom Port
    Given a redis instance with a custom port
    And a redis-store session manager
    And the redis-store session manager is configured with the redis port
    When the tomcat instance is started
    And a user starts a session
    Then the redis-store session manager should not log a connection failure message

  Scenario: Redis-Store Session Manager Connects Authorized
    Given a redis instance with a password
    And a redis-store session manager
    And the redis-store session manager is configured with the redis password
    When the tomcat instance is started
    And a user starts a session
    Then the redis-store session manager should not log a connection failure message

  Scenario: Redis-Store Session Manager Connects Unauthorized
    Given a redis instance with a password
    And a redis-store session manager
    When the tomcat instance is started
    And a user starts a session
    Then the redis-store session manager should log a connection failure message

  Scenario: Redis-Store Session Manager Connects Expecting Authorization
    Given a redis instance
    And a redis-store session manager
    And the redis-store session manager is configured with a custom password
    When the tomcat instance is started
    And a user starts a session
    Then the redis-store session manager should log a connection failure message
