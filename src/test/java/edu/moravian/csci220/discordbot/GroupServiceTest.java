package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupServiceTest {

    @Test
    void testCreateGroup() throws JsonProcessingException {
        InMemoryRedisRepository repo = new InMemoryRedisRepository();
        GroupService service = new GroupService(repo);

        Group g = service.createGroup("math");
        assertNotNull(g);
        assertEquals("math", g.getName());

        Group g2 = service.createGroup("math");
        assertSame(g, g2);
    }

    @Test
    void testJoinGroup() throws JsonProcessingException {
        InMemoryRedisRepository repo = new InMemoryRedisRepository();
        GroupService service = new GroupService(repo);

        service.joinGroup("science", "userA", "alex");

        User u = repo.getUser("userA");
        assertTrue(u.isMemberOf("science"));

        Group g = repo.getGroup("science");
        assertTrue(g.getMembers().contains("userA"));
    }
}
