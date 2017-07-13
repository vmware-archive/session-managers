Feature: Redis-Store Session Manager Session Management
  In order that sessions can be persisted across Tomcat restarts,
  an admin can store sessions in a redis-store using the redis-store session manager

  Scenario: User Session Survives Tomcat Restart
    Given a redis instance
    And a redis-store session manager
    When the tomcat instance is started
    And a user starts a session
    And the tomcat instance is restarted
    Then the user session should survive
