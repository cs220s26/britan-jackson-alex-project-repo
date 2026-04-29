package edu.moravian;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupTest {

    @Test
    void testAddMember() {
        Group g = new Group("math");

        g.addMember("user1");
        g.addMember("user1"); // idempotent

        assertEquals(1, g.getMembers().size());
        assertTrue(g.getMembers().contains("user1"));
    }

    @Test
    void testAddXP() {
        Group g = new Group("science");

        g.addXP("user1", 10);
        g.addXP("user1", 5);

        assertEquals(15, g.getLeaderboard().get("user1"));
    }
}
