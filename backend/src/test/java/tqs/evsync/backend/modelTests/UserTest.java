package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        user.setId(10L);
        user.setEmail("test@example.com");
        user.setPassword("secret");

        assertEquals(10L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
    }
}