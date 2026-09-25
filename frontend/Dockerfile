FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

# Railway injects service variables as build args for Docker builds; VITE_API_URL
# must be present at build time since Vite bakes import.meta.env.* into the bundle.
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

FROM caddy:2-alpine
WORKDIR /app

COPY Caddyfile ./
RUN caddy fmt Caddyfile --overwrite
COPY --from=build /app/dist ./dist

CMD ["caddy", "run", "--config", "Caddyfile", "--adapter", "caddyfile"]
