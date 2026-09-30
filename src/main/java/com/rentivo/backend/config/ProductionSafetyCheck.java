package com.rentivo.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** Refuses to boot in production with any development shortcut still switched on. */
@Component
public class ProductionSafetyCheck implements ApplicationRunner {

    private final Environment env;
    private final RentivoProperties props;

    public ProductionSafetyCheck(Environment env, RentivoProperties props) {
        this.env = env;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!env.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }
        if (props.otp().devMode()) {
            throw new IllegalStateException("rentivo.otp.dev-mode must be false in production");
        }
        if (props.subscriptions().devActivationEnabled()) {
            throw new IllegalStateException("rentivo.subscriptions.dev-activation-enabled must be false in production");
        }
    }
}
