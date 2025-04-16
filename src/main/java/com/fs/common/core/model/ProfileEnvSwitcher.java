package com.fs.common.core.model;

import lombok.Getter;
import lombok.Setter;

public final class ProfileEnvSwitcher {
    @Getter @Setter private static String developEnv = "local";
}
