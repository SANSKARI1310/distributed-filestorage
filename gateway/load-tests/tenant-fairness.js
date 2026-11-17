
import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const latencyA = new Trend('latency_tenant_a', true);
const latencyB = new Trend('latency_tenant_b', true);
const latencyC = new Trend('latency_tenant_c', true);

const errorsA = new Rate('errors_tenant_a');
const errorsB = new Rate('errors_tenant_b');
const errorsC = new Rate('errors_tenant_c');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1/files/upload';

const TEST_DURATION = __ENV.DURATION || '20s';

export const options = {
    scenarios: {
       
        noisy_tenant_a: {
            executor: 'constant-arrival-rate',
            rate: 150,
            timeUnit: '1s',
            duration: TEST_DURATION,
            preAllocatedVUs: 60,
            maxVUs: 240,
            exec: 'tenantA',
        },

        steady_tenant_b: {
            executor: 'constant-arrival-rate',
            rate: 90,
            timeUnit: '1s',
            duration: TEST_DURATION,
            preAllocatedVUs: 20,
            maxVUs: 80,
            exec: 'tenantB',
        },

        sensitive_tenant_c: {
            executor: 'constant-arrival-rate',
            rate: 30,
            timeUnit: '1s',
            duration: TEST_DURATION,
            preAllocatedVUs: 20,
            maxVUs: 80,
            exec: 'tenantC',
        },

    },

    // Initially collect results without enforcing performance thresholds.
    // We'll establish realistic thresholds after seeing the baseline.
};

function sendRequest(tenant, user, latency, errors) {
    const payload = JSON.stringify({
        fileName: `k6-${tenant}-${__VU}-${__ITER}-${Date.now()}.pdf`,
        fileSize: 2048,
        mimeType: 'application/pdf',
        checksumSha256: 'testsha',
    });

    const response = http.post(BASE_URL, payload, {
        headers: {
            'Content-Type': 'application/json',
            'X-ORGANIZATION-ID': tenant,
            'X-USER-ID': user,
        },
        tags: { tenant },
    });

    latency.add(response.timings.duration);
    errors.add(response.status !== 200);

    check(response, {
        'upload initialization returned 200': (r) => r.status === 200,
    });
}

export function tenantA() {
    sendRequest(
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '10000000-0000-0000-0000-000000000001',
        latencyA,
        errorsA
    );
}

export function tenantB() {
    sendRequest(
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '20000000-0000-0000-0000-000000000002',
        latencyB,
        errorsB
    );
}

export function tenantC() {
    sendRequest(
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        '30000000-0000-0000-0000-000000000003',
        latencyC,
        errorsC
    );
}
