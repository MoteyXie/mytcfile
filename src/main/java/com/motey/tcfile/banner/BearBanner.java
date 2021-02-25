package com.motey.tcfile.banner;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class BearBanner implements Banner {
    private static final String BANNER =
            "  ___ ___         .__  .__          \n" +
                    " /   |   \\   ____ |  | |  |   ____  \n" +
                    "/    ~    \\_/ __ \\|  | |  |  /  _ \\ \n" +
                    "\\    Y    /\\  ___/|  |_|  |_(  <_> )\n" +
                    " \\___|_  /  \\___  >____/____/\\____/ \n" +
                    "       \\/       \\/                  ";


    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println(BANNER);
        out.println();
    }
}
