FROM amazoncorretto:25-alpine

ENV JAVA_HOME=/usr/lib/jvm/default-jvm
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# JetBrains Gateway forcibly resets JAVA_HOME to /usr/lib/jvm/java for its remote
# backend; that path does not exist in this image, so point it at the real JDK.
RUN ln -s default-jvm /usr/lib/jvm/java

RUN apk add --no-cache \
    git \
    bash \
    curl \
    nodejs \
    npm \
    gcompat \
    gtk+3.0 \
    libxtst \
    libxrandr \
    libxi \
    mesa-gl \
    alsa-lib \
    ttf-dejavu \
    xvfb-run \
    shadow

RUN npm install -g @anthropic-ai/claude-code

RUN addgroup -S dev && adduser -S -G dev -s /bin/bash dev

WORKDIR /workspace
RUN chown -R dev:dev /workspace

USER dev
