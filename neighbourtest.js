import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom Pisces telemetry per tenant
const durationA = new Trend('duration_tenant_a');
const durationB = new Trend('duration_tenant_b');
const durationC = new Trend('duration_tenant_c');

const failRateA = new Rate('fail_rate_tenant_a');
const failRateB = new Rate('fail_rate_tenant_b');
const failRateC = new Rate('fail_rate_tenant_c');

export const options = {
  scenarios: {
    // 1. Aggressive Noisy Tenant A: Bursts hard to choke connection pools
    noisy_tenant_a: {
      executor: 'ramping-vus',
      startVUs: 10,
      stages: [
        { duration: '15s', target: 80 },
        { duration: '30s', target: 120 },
        { duration: '15s', target: 0 },
      ],
      exec: 'tenantA',
    },
    // 2. Steady Baseline Tenant B: Constant workload
    steady_tenant_b: {
      executor: 'constant-vus',
      vus: 5,
      duration: '60s',
      exec: 'tenantB',
    },
    // 3. Latency-Sensitive Tenant C: Low-rate critical traffic
    sensitive_tenant_c: {
      executor: 'constant-vus',
      vus: 2,
      duration: '60s',
      exec: 'tenantC',
    },
  },
  thresholds: {
    // Pisces Goal: Even under heavy noise from Tenant A, Tenant B & C p(95) must remain stable
    'duration_tenant_b': ['p(95)<250'],
    'duration_tenant_c': ['p(95)<150'],
    'fail_rate_tenant_b': ['rate<0.01'],
    'fail_rate_tenant_c': ['rate<0.01'],
  },
};

const BASE_URL = 'http://localhost:8080/api/v1/files/upload';

function makePayload(prefix) {
  return JSON.stringify({
    fileName: `${prefix}-file-${Date.now()}-${Math.random().toString(36).substring(7)}.pdf`,
    fileSize: 2048,
    mimeType: 'application/pdf',
    checksumSha256: 'testsha'
  });
}

// Tenant A Function (No sleep, max pressure)
export function tenantA() {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Organization-Id': 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
      'X-User-Id': '10000000-0000-0000-0000-000000000001',
    },
  };
  const res = http.post(BASE_URL, makePayload('tenant-a'), params);
  durationA.add(res.timings.duration);
  failRateA.add(res.status !== 200);
  check(res, { 'Tenant A OK': (r) => r.status === 200 });
}

// Tenant B Function (Paced with 500ms think time)
export function tenantB() {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Organization-Id': 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
      'X-User-Id': '20000000-0000-0000-0000-000000000002',
    },
  };
  const res = http.post(BASE_URL, makePayload('tenant-b'), params);
  durationB.add(res.timings.duration);
  failRateB.add(res.status !== 200);
  check(res, { 'Tenant B OK': (r) => r.status === 200 });
  sleep(0.5);
}

// Tenant C Function (Paced with 1s think time)
export function tenantC() {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Organization-Id': 'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'X-User-Id': '30000000-0000-0000-0000-000000000003',
    },
  };
  const res = http.post(BASE_URL, makePayload('tenant-c'), params);
  durationC.add(res.timings.duration);
  failRateC.add(res.status !== 200);
  check(res, { 'Tenant C OK': (r) => r.status === 200 });
  sleep(1);
}