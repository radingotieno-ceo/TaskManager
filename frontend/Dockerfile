# Multi-stage build for Angular application
# Build stage
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files for better caching
COPY package.json yarn.lock* ./

# Install dependencies using yarn
RUN yarn install --frozen-lockfile

# Copy source code
COPY . .

# Build the application using yarn
RUN yarn build

# Production stage
FROM nginx:alpine

# Install wget for health checks
RUN apk add --no-cache wget

# Copy built application
COPY --from=build /app/dist/task-management-frontend /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create non-root user
RUN addgroup -g 1001 -S nginxgroup && \
    adduser -u 1001 -S nginxuser -G nginxgroup

# Set proper permissions
RUN chown -R nginxuser:nginxgroup /usr/share/nginx/html && \
    chown -R nginxuser:nginxgroup /var/cache/nginx && \
    chown -R nginxuser:nginxgroup /var/log/nginx && \
    chown -R nginxuser:nginxgroup /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginxuser:nginxgroup /var/run/nginx.pid

# Switch to non-root user
USER nginxuser

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]


