package com.taxi.user.support;

import org.testcontainers.DockerClientFactory;

public final class TestEnv {

    private TestEnv() {
    }

    public static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }
}
