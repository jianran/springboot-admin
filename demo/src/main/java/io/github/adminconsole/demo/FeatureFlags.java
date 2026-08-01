package io.github.adminconsole.demo;

import io.github.adminconsole.feature.FeatureFlag;
import org.springframework.stereotype.Component;

@Component("featureFlags")
public class FeatureFlags {
    @FeatureFlag(description = "Enables the redesigned checkout flow")
    private boolean checkoutEnabled = true;
    public boolean isCheckoutEnabled() { return checkoutEnabled; }
}
