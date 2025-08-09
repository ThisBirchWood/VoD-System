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
    profilePictureUrl: string
}

type Clip = {
    id: number,
    userId: number,
    title: string,
    description: string,
    duration: number,
    createdAt: string,
}

type ProgressResult = {
    process: {
        progress: number,
        complete: boolean
    };
    conversion: {
        progress: number,
        complete: boolean
    };
};

export type {
    APIResponse,
    VideoMetadata,
    User,
    Clip,
    ProgressResult
}