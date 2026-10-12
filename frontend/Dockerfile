# ---------- build ----------
FROM node:20.18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
# npm can exit 0 after a failed download, leaving node_modules incomplete; check the
# build tools are there so a broken install fails here instead of being cached
RUN npm ci --no-audit --no-fund --fetch-retries=5 \
 && test -x node_modules/.bin/tsc && test -x node_modules/.bin/vite
COPY . .
RUN npm run build

# ---------- runtime ----------
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
