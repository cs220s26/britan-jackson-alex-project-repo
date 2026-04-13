package edu.moravian.csci220.discordbot;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelScopeTest {

  @Test
  void initiallyNotBound() {
    var scope = scope();
    assertFalse(scope.isBound());
  }

  @Test
  void matchesBoundChannel_falseWhenNotBound() {
    var scope = scope();
    assertFalse(scope.matchesBoundChannel(1L));
  }

  @Test
  void matchesBoundChannel_trueWhenBoundAndIdMatches() throws Exception {
    var scope = scope();
    setBoundTextChannelId(scope, 42L);
    assertTrue(scope.isBound());
    assertTrue(scope.matchesBoundChannel(42L));
  }

  @Test
  void matchesBoundChannel_falseWhenBoundAndIdDiffers() throws Exception {
    var scope = scope();
    setBoundTextChannelId(scope, 42L);
    assertFalse(scope.matchesBoundChannel(99L));
  }

  private static ChannelScope scope() {
    var cfg = new BotConfiguration("token", Optional.empty());
    return new ChannelScope(cfg, List.of());
  }

  private static void setBoundTextChannelId(ChannelScope scope, long id) throws Exception {
    Field f = ChannelScope.class.getDeclaredField("boundTextChannelId");
    f.setAccessible(true);
    f.setLong(scope, id);
  }
}
