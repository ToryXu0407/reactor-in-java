package com.toryxu.reactor;

import java.util.Objects;

/**
 * @author toryxu
 * @version 1.0
 * @date 2020/9/24 11:24 下午
 */
public class Player {

    public String firstName;

    public String lastName;

    public Player(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(firstName, player.firstName) &&
                Objects.equals(lastName, player.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
