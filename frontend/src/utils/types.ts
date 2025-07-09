type VideoMetadata = {
    title: string,
    description: string,
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
    email: string,
    profilePicture: string
}

type Clip = {
    title: string,
    description: string,
    duration: number,
    thumbnailPath: string,
    videoPath: string,
    fps: number,
    width: number,
    height: number,
    createdAt: string,
}

export type {
    APIResponse,
    VideoMetadata,
    User,
    Clip
}