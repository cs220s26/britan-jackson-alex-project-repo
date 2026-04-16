package edu.moravian;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testAddXP() {
        User u = new User("1", "alex");
        u.addXP(20);
        u.addXP(10);
        assertEquals(30, u.getTotalXP());
    }

    @Test
    void testAddGroup() {
        User u = new User("1", "alex");
        u.addGroup("math");
        u.addGroup("math"); // idempotent
        assertEquals(1, u.getGroups().size());
    }
}
