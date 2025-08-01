package tech.derbent.base.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test for DatabaseResetService to ensure it handles missing data.sql gracefully.
 */
@SpringBootTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create"})
public class DatabaseResetServiceTest {

    @Autowired
    private DatabaseResetService databaseResetService;

    @Test
    public void testDatabaseResetWithoutDataSql() {
        // This should not throw any exceptions when data.sql doesn't exist
        assertDoesNotThrow(() -> databaseResetService.resetDatabase());
    }
}