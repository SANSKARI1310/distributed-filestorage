
package com.example.demo.fair;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TenantWeightResolver {

    private static final Map<String, Integer> TENANT_WEIGHTS = Map.of(
        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 5,
        "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", 3,
        "cccccccc-cccc-cccc-cccc-cccccccccccc", 1
    );

    public int getWeight(String tenantId) {
        return TENANT_WEIGHTS.getOrDefault(tenantId, 1);
    }
}
