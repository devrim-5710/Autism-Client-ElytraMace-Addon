package com.autism.elytramace;

import autismclient.api.ApiVersion;
import autismclient.api.SimpleAddon;
import com.autism.elytramace.modules.ElytraMaceModule;

public final class ElytraMaceAddon extends SimpleAddon {
    public static final String ID = "elytra-mace-addon";

    public ElytraMaceAddon() {
        super(ApiVersion.CURRENT, "com.autism.elytramace");
    }

    @Override
    protected void initialize() {
        registerModule(new ElytraMaceModule());
    }
}
