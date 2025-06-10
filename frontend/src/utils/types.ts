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

export type {
    APIResponse,
    VideoMetadata
}