import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. Configure the Load Profile & Pass/Fail Thresholds
export const options = {
  stages: [
    { duration: '10s', target: 50 }, // Ramp up to 50 Virtual Users (VUs)
    { duration: '30s', target: 50 }, // Hold steady at 50 VUs
    { duration: '10s', target: 0 },  // Ramp down to 0 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete under 500ms
    http_req_failed: ['rate<0.01'],   // Less than 1% of requests can fail
  },
};


// 2. Define the Virtual User Action
export default function () {
  // Target the Gateway to test Load Balancing, or change to 8081 to bypass it
  const url = 'http://localhost:8080/api/v1/files/upload';

  // Generate a unique filename per request to avoid caching/collision edge cases
  const payload = JSON.stringify({
    fileName: `report-${__VU}-${__ITER}.pdf`, 
    fileSize: 2048,
    mimeType: 'application/pdf',
    checksumSha256: 'testsha'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Organization-Id': '11111111-1111-1111-1111-111111111111',
      'X-User-Id': '22222222-2222-2222-2222-222222222222',
    },
  };

  // 3. Execute and Validate
  const res = http.post(url, payload, params);

  check(res, {
    'is status 200': (r) => r.status === 200,
    'has fileId': (r) => r.json('fileId') !== undefined,
  });

  if (res.status !== 200) {
  console.log(`[HTTP ${res.status}] Failed Request Body: ${res.body}`);
}

check(res, {
  'is status 200': (r) => r.status === 200,
  'has fileId': (r) => r.json('fileId') !== undefined,
});
  // Brief pause to simulate human interaction intervals
  sleep(1);
}