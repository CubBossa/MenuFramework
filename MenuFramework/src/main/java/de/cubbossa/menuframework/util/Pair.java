package de.cubbossa.menuframework.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<L, R> {

    private L left;
    private R right;
}
