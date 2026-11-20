# Local dev (V0 skeleton)

- Backend: `./mvnw -pl backend/application spring-boot:run` (health on `/actuator/health` only).
- Frontend: `pnpm --dir frontend/app start` after `pnpm install` at repo root.
- Full stack compose skeleton: `docker compose -f compose.yaml up` (synthetic credentials only).

LAN-only; do not expose beyond local loopback without acceptance controller approval.