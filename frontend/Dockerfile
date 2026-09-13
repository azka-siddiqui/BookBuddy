# ---- Build stage ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
# The app talks to the gateway via the nginx /api proxy, so use a relative base.
ENV VITE_API_BASE_URL=""
RUN npm run build

# ---- Runtime stage ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
