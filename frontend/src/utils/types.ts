type VideoMetadata = {
    startPoint: number,
    endPoint: number,
    fps: number,
    width: number,
    height: number,
    fileSize: number
}

type APIResponse = {
    status: string,
    data: any,
    message: string
}

type User = {
    name: string,
    email: string
}

export type {
    APIResponse,
    VideoMetadata,
    User
}