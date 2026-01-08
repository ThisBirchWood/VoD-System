# Overview
The VoD System is a powerful clip management platform designed to streamline how you handle your video content. Whether you're a content creator, streamer, or educator, VoD System lets you:

- Upload clips effortlessly and securely.
- Edit and trim videos with intuitive controls.
- Compress files to specific file sizes.
- Organize your clips for quick access and sharing.

This VoD system began as a small project back in my 5th year of secondary school, during lockdown. I wanted a quick way to send game clips to Discord, but the 8MB file size limit made it frustrating to manually tweak bitrates for every clip. To solve that, I wrote a simple Python script to automate the process. What started as a quick fix has since evolved into a full-fledged system — now built with a Spring Boot backend and a modern React/Vite frontend.

## Requirements
- Docker & Docker Compose
- JDK 21+
- Node.js 16+
- FFMPEG
- Google Client keys
## Steps
1. Create your own two `.env` files based off the `example.env` files in the root and in the frontend folder 
2. Spin up docker container with `docker compose up`
3. Run `./mvnw clean package` to build the jar
4. Run `./mvnw spring-boot:run` to run the backend section
5. Run `cd frontend && npm install && npm run dev` to build and run the frontend
6. Endpoints should be available at 8080 (backend) and 5173 (frontend)

# Future Plans
- **Format Handling**
  - Backend conversion of non-MP4 files via FFMPEG for broader format support
- **Input Sources**
    - Support for local file uploads and YouTube imports
- **Testing**
    - Unit tests, including coverage for FFMPEG-related functionality
- **API Documentation**
  - Comprehensive and maintainable backend API docs
