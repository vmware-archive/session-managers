import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

appender('LOG', FileAppender) {
    file = 'build/test-results/cuke.log'
    append = false
    encoder(PatternLayoutEncoder) {
        pattern = '[%d{HH:mm:ss.SSS}][%.-3level] %msg%n'
    }
    filter(ThresholdFilter) {
        level = ch.qos.logback.classic.Level.INFO
    }
}

appender('DEBUG', FileAppender) {
    file = 'build/test-results/cuke-debug.log'
    append = false
    encoder(PatternLayoutEncoder) {
        pattern = '[%d{HH:mm:ss.SSS}][%.-3level] %logger: %msg%n'
    }
    filter(ThresholdFilter) {
        level = ch.qos.logback.classic.Level.DEBUG
    }
}

logger('io.pivotal', ch.qos.logback.classic.Level.DEBUG, ['LOG', 'DEBUG'])
