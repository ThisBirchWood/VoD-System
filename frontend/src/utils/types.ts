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
    id: number,
    userId: number,
    title: string,
    description: string,
    duration: number,
    createdAt: string,
}

export type {
    APIResponse,
    VideoMetadata,
    User,
    Clip
}