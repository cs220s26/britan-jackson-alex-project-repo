package edu.moravian;

import com.fasterxml.jackson.core.JsonProcessingException;

public class GroupService {

    private final RedisRepository repo;

    public GroupService(RedisRepository repo) {
        this.repo = repo;
    }

    public Group createGroup(String name) throws JsonProcessingException {
        Group g = repo.getGroup(name);
        if (g != null) {
            return g; // already exists
        }
        Group group = new Group(name);
        repo.saveGroup(group);
        return group;
    }

    public void joinGroup(String groupName, String userId, String username) throws JsonProcessingException {
        Group g = repo.getGroup(groupName);
        if (g == null) {
            g = createGroup(groupName);
        }

        g.addMember(userId);

        // ensure user exists
        User user = repo.getUser(userId);
        if (user == null) {
            user = new User(userId, username);
        }

        user.addGroup(groupName);

        repo.saveUser(user);
        repo.saveGroup(g);
    }
}
